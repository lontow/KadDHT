package org.kaddht.kademlia.message;

import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.node.Node;

import java.io.IOException;
import java.util.List;

/**
 * 接受 NodeLookupMessage 和 回复 NodeReplyMessage
 *
 * @author Lontow
 * @created 20201015
 */
public class NodeLookupReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KadConfiguration config;

    public NodeLookupReceiver(KadServer server, KademliaNode local, KadConfiguration config)
    {
        this.server = server;
        this.localNode = local;
        this.config = config;
    }

    /**
     * 处理 NodeLookupMessage
     *
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        NodeLookupMessage msg = (NodeLookupMessage) incoming;

        Node origin = msg.getOrigin();

        /* 将发送请求的节点加入路由表 */
        this.localNode.getRoutingTable().insert(origin);

        /* 找到距 LookupId 最近的 k个节点*/
        List<Node> nodes = this.localNode.getRoutingTable().findClosest(msg.getLookupId(), this.config.k());

        /* 回复消息 */
        Message reply = new NodeReplyMessage(this.localNode.getNode(), nodes);

        if (this.server.isRunning())
        {
            /* 回复 */
            this.server.reply(origin, reply, comm);
        }
    }

    /**
     * 超时函数
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public void timeout(int comm) throws IOException
    {
    }
}
