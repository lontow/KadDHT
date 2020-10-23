/**
 * @author 刘朕龙
 * @created 20201020
 * @desc 使用引导节点处理连接到现有 Kademlia 网络的操作
 */
package org.kaddht.kademlia.operation;

import org.kaddht.kademlia.message.Receiver;
import java.io.IOException;

import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.exceptions.RoutingException;
import org.kaddht.kademlia.message.AcknowledgeMessage;
import org.kaddht.kademlia.message.ConnectMessage;
import org.kaddht.kademlia.message.Message;
import org.kaddht.kademlia.node.Node;

public class ConnectOperation implements Operation, Receiver
{

    public static final int MAX_CONNECT_ATTEMPTS = 5;
     // 连接每个节点尝试五次

    private final KadServer server;
    private final KademliaNode localNode;
    private final Node bootstrapNode;
    private final KadConfiguration config;

    private boolean error;
    private int attempts;

    /**
     * @param server    用于发送/接收消息的消息服务器
     * @param local     The local node
     * @param bootstrap 用于将本地节点引导到网络上的节点
     * @param config
     */
    public ConnectOperation(KadServer server, KademliaNode local, Node bootstrap, KadConfiguration config)
    {
        this.server = server;
        this.localNode = local;
        this.bootstrapNode = bootstrap;
        this.config = config;
    }

    @Override
    public synchronized void execute() throws IOException
    {
        try
        {
            // 联系引导节点
            this.error = true;
            this.attempts = 0;
            Message m = new ConnectMessage(this.localNode.getNode());

            // 将连接消息发送到引导节点
            server.sendMessage(this.bootstrapNode, m, this);

            // 最长等待config.operationTimeout（）时间
            int totalTimeWaited = 0;
            int timeInterval = 50;
                // 我们每300毫秒重新检查一次
            while (totalTimeWaited < this.config.operationTimeout())
            {
                if (error)
                {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                }
                else
                {
                    break;
                }
            }
            if (error)
            {
                // 超时
                throw new RoutingException("ConnectOperation: Bootstrap node did not respond: " + bootstrapNode);
            }

            // 查找我们自己的ID，以使节点离我们更近
            Operation lookup = new NodeLookupOperation(this.server, this.localNode, this.localNode.getNode().getNodeId(), this.config);
            lookup.execute();

            /**
             * 刷新存储桶以获得良好的路由表
             * 完成上述查找操作后，K个节点将位于我们的路由表中
             * 尝试填充所有存储桶
             */
            new BucketRefreshOperation(this.server, this.localNode, this.config).execute();
        }
        catch (InterruptedException e)
        {
            System.err.println("Connect operation was interrupted. ");
        }
    }

    /**
     * 从引导节点接收AcknowledgeMessage
     *
     * @param comm
     */
    @Override
    public synchronized void receive(Message incoming, int comm)
    {
        // 接受确认消息
        AcknowledgeMessage msg = (AcknowledgeMessage) incoming;

        // 引导节点响应后，将其插入我们的空间
        this.localNode.getRoutingTable().insert(this.bootstrapNode);

        // 得到回应
        error = false;

        // 唤醒等待的进程
        notify();
    }

    /**
     * 连接消息最多重新发送到引导节点 MAX_ATTEMPTS 次
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public synchronized void timeout(int comm) throws IOException
    {
        if (++this.attempts < MAX_CONNECT_ATTEMPTS)
        {
            this.server.sendMessage(this.bootstrapNode, new ConnectMessage(this.localNode.getNode()), this);
        }
        else
        {
            notify();
        }
    }
}
