package org.kaddht.kademlia.operation;

import java.io.IOException;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.node.KademliaId;

/**
 * 在每个时间间隔t，节点需要刷新其 K-Buckets
 * 此操作需要刷新此节点的 K-Buckets
 *
 * @author 张文令
 * @created 20201019
 */
public class BucketRefreshOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KadConfiguration config;

    public BucketRefreshOperation(KadServer server, KademliaNode localNode, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    /**
     * 每个存储桶都需要在每个时间间隔t刷新
     * 在每个存储桶范围内找到一个标识符，使用它查找最接近该标识符的节点
     * 允许刷新存储桶
     * 然后对每个生成的NodeId执行NodeLookupOperation，这将找到该ID的K最近节点，并更新必要的K桶
     *
     *
     * @throws java.io.IOException
     */
    @Override
    public synchronized void execute() throws IOException
    {
        for (int i = 1; i < KademliaId.ID_LENGTH; i++)
        {
            // 构造一个与当前节点 ID 相距 i 位的 NodeId
            final KademliaId current = this.localNode.getNode().getNodeId().generateNodeIdByDistance(i);

            // 运行节点查找操作，在不同的线程中运行以加快速度
            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        new NodeLookupOperation(server, localNode, current, BucketRefreshOperation.this.config).execute();
                    }
                    catch (IOException e)
                    {
                        //System.err.println("Bucket Refresh Operation Failed. Msg: " + e.getMessage());
                    }
                }
            }.start();
        }
    }
}
