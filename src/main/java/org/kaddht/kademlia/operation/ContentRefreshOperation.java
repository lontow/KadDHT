package org.kaddht.kademlia.operation;

import java.io.IOException;
import java.util.List;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.dht.KademliaDHT;
import org.kaddht.kademlia.dht.KademliaStorageEntryMetadata;
import org.kaddht.kademlia.dht.StorageEntryMetadata;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.message.Message;
import org.kaddht.kademlia.message.StoreContentMessage;
import org.kaddht.kademlia.node.Node;

/**
 * 通过将数据发送到数据的K-Closest节点来刷新/恢复该节点上的数据
 *
 * @author 张文令
 * @since 20201010
 */
public class ContentRefreshOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;
    private final KadConfiguration config;

    public ContentRefreshOperation(KadServer server, KademliaNode localNode, KademliaDHT dht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
    }

    /**
     * 对于此 DHT上存储的每个内容，将其分发到 K个最近的节点。
     * 如果此节点不再是K个最近的节点之一，则删除该内容。
     * 此时假设我们的 KadRoutingTable 已更新，我们可以从该表中获取K个最近的节点
     *
     * @throws java.io.IOException
     */
    @Override
    public void execute() throws IOException
    {
        // 获取内容的所有存储条目的列表
        List<KademliaStorageEntryMetadata> entries = this.dht.getStorageEntries();

        // 如果某个内容在此时间之前最后一次重新上传，那么我们需要重新上传它
        final long minRepublishTime = (System.currentTimeMillis() / 1000L) - this.config.restoreInterval();

        // 分发每个存储条目
        for (KademliaStorageEntryMetadata e : entries)
        {
            // 检查此条目的上次更新时间，仅在最近一次 >1小时之前将其分发
            if (e.lastRepublished() > minRepublishTime)
            {
                continue;
            }

            // 设置此内容现在重新上传
            e.updateLastRepublished();

            // 获取最接近该条目的K个节点
            List<Node> closestNodes = this.localNode.getRoutingTable().findClosest(e.getKey(), this.config.k());

            // 创建信息
            Message msg = new StoreContentMessage(this.localNode.getNode(), dht.get(e));

            // 将消息存储在所有K节点上
            for (Node n : closestNodes)
            {
                // 无须存储，因为已经存在
                if (!n.equals(this.localNode.getNode()))
                {
                    // 将内容存储库操作发送到K-Closest节点
                    this.server.sendMessage(n, msg, null);
                }
            }

            // 删除此节点上不是该K-Closest节点之一的任何内容
            try
            {
                if (!closestNodes.contains(this.localNode.getNode()))
                {
                    this.dht.remove(e);
                }
            }
            catch (ContentNotFoundException cnfe)
            {
                System.err.println("ContentRefreshOperation: Removing content from local node, content not found... Message: " + cnfe.getMessage());
            }
        }

    }
}
