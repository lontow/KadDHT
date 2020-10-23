package org.kaddht.kademlia.operation;

import org.kaddht.kademlia.message.Receiver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.exceptions.RoutingException;
import org.kaddht.kademlia.message.Message;
import org.kaddht.kademlia.message.NodeLookupMessage;
import org.kaddht.kademlia.message.NodeReplyMessage;
import org.kaddht.kademlia.node.KeyComparator;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.node.KademliaId;

/**
 * 查找最接近指定标识符的K个节点
 * 算法得到K个最接近节点的响应时终止
 * 无法响应的节点则删除
 *
 * @author 刘朕龙
 * @created 20201017
 */
public class NodeLookupOperation implements Operation, Receiver
{

    private static final String UNASKED = "UnAsked";
    private static final String AWAITING = "Awaiting";
    private static final String ASKED = "Asked";
    private static final String FAILED = "Failed";

    private final KadServer server;
    private final KademliaNode localNode;
    private final KadConfiguration config;

    private final Message lookupMessage;

    private final Map<Node, String> nodes;

    /**
     *  跟踪传输中的消息并等待回复
     */
    private final Map<Integer, Node> messagesTransiting;

    /**
     *  用于排序节点
     */
    private final Comparator comparator;

    
    {
        messagesTransiting = new HashMap<>();
    }

    /**
     * @param server    KadServer用于通讯
     * @param localNode 进行通讯的本地节点
     * @param lookupId  查找附近节点的ID
     * @param config
     */
    public NodeLookupOperation(KadServer server, KademliaNode localNode, KademliaId lookupId, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.config = config;

        this.lookupMessage = new NodeLookupMessage(localNode.getNode(), lookupId);


        this.comparator = new KeyComparator(lookupId);
        this.nodes = new TreeMap(this.comparator);
    }

    /**
     * @throws java.io.IOException
     * @throws org.kaddht.kademlia.exceptions.RoutingException
     */
    @Override
    public synchronized void execute() throws IOException, RoutingException
    {
        try
        {
            // 按照要求设置本地节点
            nodes.put(this.localNode.getNode(), ASKED);


            this.addNodes(this.localNode.getRoutingTable().getAllNodes());

            int totalTimeWaited = 0;
            int timeInterval = 10;
             // 我们每n毫秒重新检查一次
            while (totalTimeWaited < this.config.operationTimeout())
            {
                if (!this.askNodesorFinish())
                {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                }
                else
                {
                    break;
                }
            }

            // 完成后更新路由表
            this.localNode.getRoutingTable().setUnresponsiveContacts(this.getFailedNodes());

        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<Node> getClosestNodes()
    {
        return this.closestNodes(ASKED);
    }

    /**
     * 将列表中的节点添加到要查找的节点集中
     *
     * @param list 从中添加节点的列表
     */
    public void addNodes(List<Node> list)
    {
        for (Node o : list)
        {
            // 如果此节点不在列表中，请添加该节点
            if (!nodes.containsKey(o))
            {
                nodes.put(o, UNASKED);
            }
        }
    }

    /**
     * 询问已找到但尚未查询的 K 个最接近的节点
     * 确保一次传输的消息不超过DefaultConfiguration.CONCURRENCY
     * 每当接收到答复或发生超时时，都应调用此方法
     * 如果已经询问了所有K个最近的节点，并且没有传输中的消息，则算法完成
     *
     * @return <code>true</code> if finished OR <code>false</code> otherwise
     */
    private boolean askNodesorFinish() throws IOException
    {
        // 如果 >= CONCURRENCY节点在传输中，则不执行任何操作
        if (this.config.maxConcurrentMessagesTransiting() <= this.messagesTransiting.size())
        {
            return false;
        }

        // 获取未失败的K个最接近的未查询节点
        List<Node> unasked = this.closestNodesNotFailed(UNASKED);

        if (unasked.isEmpty() && this.messagesTransiting.isEmpty())
        {

            return true;
        }

        /**
         * 发送消息到列表中的节点
         * 确保正在传输的消息不超过CONCURRENT条
         */
        for (int i = 0; (this.messagesTransiting.size() < this.config.maxConcurrentMessagesTransiting()) && (i < unasked.size()); i++)
        {
            Node n = (Node) unasked.get(i);

            int comm = server.sendMessage(n, lookupMessage, this);

            this.nodes.put(n, AWAITING);
            this.messagesTransiting.put(comm, n);
        }

        return false;
    }

    /**
     * @param status
     *
     * @return 给定状态下与目标lookupId最接近的 K 个节点
     */
    private List<Node> closestNodes(String status)
    {
        List<Node> closestNodes = new ArrayList<>(this.config.k());
        int remainingSpaces = this.config.k();

        for (Map.Entry e : this.nodes.entrySet())
        {
            if (status.equals(e.getValue()))
            {

                closestNodes.add((Node) e.getKey());
                if (--remainingSpaces == 0)
                {
                    break;
                }
            }
        }

        return closestNodes;
    }

    /**
     * 给定尚未找到目标的，与目标lookupId最接近的K个节点。
     * 从那些K中获得具有指定状态的那些
     *
     * @param status The status of the nodes to return
     *
     * @return A List of the closest nodes
     */
    private List<Node> closestNodesNotFailed(String status)
    {
        List<Node> closestNodes = new ArrayList<>(this.config.k());
        int remainingSpaces = this.config.k();

        for (Map.Entry<Node, String> e : this.nodes.entrySet())
        {
            if (!FAILED.equals(e.getValue()))
            {
                if (status.equals(e.getValue()))
                {

                    closestNodes.add(e.getKey());
                }

                if (--remainingSpaces == 0)
                {
                    break;
                }
            }
        }

        return closestNodes;
    }

    /**
     * 接收并处理传入的NodeReplyMessage
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public synchronized void receive(Message incoming, int comm) throws IOException
    {
        if (!(incoming instanceof NodeReplyMessage))
        {

            return;
        }
        // 我们收到带有一组节点的NodeReplyMessage
        NodeReplyMessage msg = (NodeReplyMessage) incoming;

        // 将原始节点添加到我们的路由表中
        Node origin = msg.getOrigin();
        this.localNode.getRoutingTable().insert(origin);

        // 设置我们已经完成了对原始节点的询问
        this.nodes.put(origin, ASKED);

        // 从消息传输中删除已完成的消息
        this.messagesTransiting.remove(comm);

        // 将收到的节点添加到我们的节点列表中以进行查询
        this.addNodes(msg.getNodes());
        this.askNodesorFinish();
    }

    /**
     * 节点没有响应或数据包丢失，我们将此节点设置为丢失
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public synchronized void timeout(int comm) throws IOException
    {
        // 获取与此通信关联的节点
        Node n = this.messagesTransiting.get(comm);

        if (n == null)
        {
            return;
        }

        // 将此节点标记为丢失，并通知路由表它没有响应
        this.nodes.put(n, FAILED);
        this.localNode.getRoutingTable().setUnresponsiveContact(n);
        this.messagesTransiting.remove(comm);

        this.askNodesorFinish();
    }

    public List<Node> getFailedNodes()
    {
        List<Node> failedNodes = new ArrayList<>();

        for (Map.Entry<Node, String> e : this.nodes.entrySet())
        {
            if (e.getValue().equals(FAILED))
            {
                failedNodes.add(e.getKey());
            }
        }

        return failedNodes;
    }
}
