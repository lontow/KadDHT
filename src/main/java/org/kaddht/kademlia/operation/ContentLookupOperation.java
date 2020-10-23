package org.kaddht.kademlia.operation;

import org.kaddht.kademlia.dht.KadStorageEntry;
import org.kaddht.kademlia.message.Receiver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.dht.GetParameter;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.exceptions.RoutingException;
import org.kaddht.kademlia.exceptions.UnknownMessageException;
import org.kaddht.kademlia.message.ContentLookupMessage;
import org.kaddht.kademlia.message.ContentMessage;
import org.kaddht.kademlia.message.Message;
import org.kaddht.kademlia.message.NodeReplyMessage;
import org.kaddht.kademlia.node.KeyComparator;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.util.RouteLengthChecker;

/**
 * 查找指定的标识符并返回与其关联的值
 *
 * @author 刘朕龙
 * @since 20201019
 */
public class ContentLookupOperation implements Operation, Receiver
{

    private static final Byte UNASKED = (byte) 0x00;
    private static final Byte AWAITING = (byte) 0x01;
    private static final Byte ASKED = (byte) 0x02;
    private static final Byte FAILED = (byte) 0x03;

    private final KadServer server;
    private final KadPeer localNode;
    private KadStorageEntry contentFound = null;
    private final KadConfiguration config;

    private final ContentLookupMessage lookupMessage;

    private boolean isContentFound;

    private final SortedMap<Node, Byte> nodes;

    // 跟踪传输中的消息并等待回复
    private final Map<Integer, Node> messagesTransiting;

    // 用于排序节点
    private final Comparator comparator;

    // 统计信息
    private final RouteLengthChecker routeLengthChecker;

    
    {
        messagesTransiting = new HashMap<>();
        isContentFound = false;
        routeLengthChecker = new RouteLengthChecker();
    }

    /**
     * @param server
     * @param localNode
     * @param params    搜索所需内容的参数
     * @param config
     */
    public ContentLookupOperation(KadServer server, KadPeer localNode, GetParameter params, KadConfiguration config)
    {
        // 构造查询消息
        this.lookupMessage = new ContentLookupMessage(localNode.getNode(), params);

        this.server = server;
        this.localNode = localNode;
        this.config = config;

        /**
         * 我们初始化一个TreeMap来存储节点
         * 此 map 将根据最接近lookupId的节点进行排序
         */
        this.comparator = new KeyComparator(params.getKey());
        this.nodes = new TreeMap(this.comparator);
    }

    /**
     * @throws java.io.IOException,org.kaddht.kademlia.exceptions.RoutingException
     */
    @Override
    public synchronized void execute() throws IOException, RoutingException
    {
        try
        {
            // 按照要求设置本地节点
            nodes.put(this.localNode.getNode(), ASKED);

            /**
             * 在此处添加所有节点
             */
            List<Node> allNodes = this.localNode.getRoutingTable().getAllNodes();
            this.addNodes(allNodes);
            
            // 将初始节点集添加到routeLengthChecker
            this.routeLengthChecker.addInitialNodes(allNodes);

            /**
             * 未找到请求的内容，继续尝试直到config.operationTimeout（）时间到期
             */
            int totalTimeWaited = 0;
            int timeInterval = 10;
                // 我们每n毫秒重新检查一次
            while (totalTimeWaited < this.config.operationTimeout())
            {
                if (!this.askNodesorFinish() && !isContentFound)
                {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                }
                else
                {
                    break;
                }
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将列表中的节点添加到要查找的节点集中
     *
     * @param list The list from which to add nodes
     */
    public void addNodes(List<Node> list)
    {
        for (Node o : list)
        {
            if (!nodes.containsKey(o))
            {
                nodes.put(o, UNASKED);
            }
        }
    }

    /**
     * 询问已找到但尚未查询的K个最接近的节点。
     * 确保一次传输的消息不超过DefaultConfiguration.CONCURRENCY。
     * 每当接收到答复或发生超时时，都应调用此方法。
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
            // 所有节点已询问，所有信息已传输
            return true;
        }

        // 根据条件对节点进行排序
        Collections.sort(unasked, this.comparator);

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

        // 未完成
        return false;
    }

    /**
     * 给定尚未找到目标的，与目标lookupId最接近的K个节点。
     * 从那些 K桶中获得具有指定状态的那些
     *
     * @param status 要返回的节点的状态
     *
     * @return 最近的节点列表
     */
    private List<Node> closestNodesNotFailed(Byte status)
    {
        List<Node> closestNodes = new ArrayList<>(this.config.k());
        int remainingSpaces = this.config.k();

        for (Map.Entry e : this.nodes.entrySet())
        {
            if (!FAILED.equals(e.getValue()))
            {
                if (status.equals(e.getValue()))
                {
                    closestNodes.add((Node) e.getKey());
                }

                if (--remainingSpaces == 0)
                {
                    break;
                }
            }
        }

        return closestNodes;
    }

    @Override
    public synchronized void receive(Message incoming, int comm) throws IOException, RoutingException
    {
        if (this.isContentFound)
        {
            return;
        }

        if (incoming instanceof ContentMessage)
        {
            // 收到回复是带有所需内容的内容消息
            ContentMessage msg = (ContentMessage) incoming;

            // 将原始节点添加到我们的路由表中
            this.localNode.getRoutingTable().insert(msg.getOrigin());

            // 获取内容并检查其是否满足必需的参数
            KadStorageEntry content = msg.getContent();
            System.out.println("get Message"+content);
            this.contentFound = content;
            this.isContentFound = true;
        }
        else
        {
            // 收到的回复是一个NodeReplyMessage，其节点与所需内容最接近
            NodeReplyMessage msg = (NodeReplyMessage) incoming;

            // 将原始节点添加到我们的路由表中
            Node origin = msg.getOrigin();
            this.localNode.getRoutingTable().insert(origin);

            // 设置我们已经完成了对原始节点的询问
            this.nodes.put(origin, ASKED);

            // 从消息传输中删除已完成的该消息
            this.messagesTransiting.remove(comm);
            
            // 将收到的节点添加到routeLengthChecker
            this.routeLengthChecker.addNodes(msg.getNodes(), origin);

            // 将收到的节点添加到我们的节点列表中以进行查询
            this.addNodes(msg.getNodes());
            this.askNodesorFinish();
        }
    }

    /**
     * 节点没有响应或数据包丢失，将此节点设置为失败
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public synchronized void timeout(int comm) throws IOException
    {
        // 获取与此通信关联的节点
        Node n = this.messagesTransiting.get(new Integer(comm));

        if (n == null)
        {
            throw new UnknownMessageException("Unknown comm: " + comm);
        }

        // 将此节点标记为失败，并通知路由表无响应
        this.nodes.put(n, FAILED);
        this.localNode.getRoutingTable().setUnresponsiveContact(n);
        this.messagesTransiting.remove(comm);

        this.askNodesorFinish();
    }
    
    /**
     * @return 是否找到内容
     */
    public boolean isContentFound()
    {
        return this.isContentFound;
    }

    /**
     * @return 查找操作期间找到的所有内容的列表
     *
     * @throws org.kaddht.kademlia.exceptions.ContentNotFoundException
     */
    public synchronized KadStorageEntry getContentFound() throws ContentNotFoundException
    {
        if (this.isContentFound)
        {
            return this.contentFound;
        }
        else
        {
            throw new ContentNotFoundException("No Value was found for the given key.");
        }
    }

    /**
     * @return 到达内容所花费的跳数
     */
    public int routeLength()
    {
        return this.routeLengthChecker.getRouteLength();
    }
}
