package org.kaddht.kademlia.operation;

import java.io.IOException;
import java.util.List;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.dht.KadStorageEntry;
import org.kaddht.kademlia.dht.KademliaDHT;
import org.kaddht.kademlia.message.Message;
import org.kaddht.kademlia.message.StoreContentMessage;
import org.kaddht.kademlia.node.Node;

/**
 * 将DHT内容存储到距离内容 key 最近的K个节点上的操作
 *
 * @author 刘朕龙
 * @since 20201018
 */
public class StoreOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KadStorageEntry storageEntry;
    private final KademliaDHT localDht;
    private final KadConfiguration config;

    /**
     * @param server
     * @param localNode
     * @param storageEntry The content to be stored on the DHT
     * @param localDht     The local DHT
     * @param config
     */
    public StoreOperation(KadServer server, KademliaNode localNode, KadStorageEntry storageEntry, KademliaDHT localDht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.storageEntry = storageEntry;
        this.localDht = localDht;
        this.config = config;
    }

    @Override
    public synchronized void execute() throws IOException
    {
        // 获取我们需要在其上存储内容的节点
        NodeLookupOperation ndlo = new NodeLookupOperation(this.server, this.localNode, this.storageEntry.getContentMetadata().getKey(), this.config);
        ndlo.execute();
        List<Node> nodes = ndlo.getClosestNodes();

        // 构造信息
        Message msg = new StoreContentMessage(this.localNode.getNode(), this.storageEntry);

        // 将消息存储在所有K节点上K-Nodes
        for (Node n : nodes) {
            if (n.equals(this.localNode.getNode()))
            {
                // 本地存储内容
                this.localDht.store(this.storageEntry);
            }
            else
            {
                /**
                 * @todo Create a receiver that receives a store acknowledgement message to count how many nodes a content have been stored at
                 */
                this.server.sendMessage(n, msg, null);
            }
        }
    }

    /**
     * @return 已存储此内容的节点数
     *
     * @todo Implement this method
     */
    public int numNodesStoredAt()
    {
        return 1;
    }
}
