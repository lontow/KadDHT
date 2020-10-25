package org.kaddht.kademlia;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.kaddht.kademlia.dht.GetParameter;
import org.kaddht.kademlia.dht.KadStorageEntry;
import org.kaddht.kademlia.dht.KadContent;
import org.kaddht.kademlia.dht.KademliaDHT;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.exceptions.RoutingException;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.routing.KademliaRoutingTable;

/**
 * 网络上的主要Kademlia节点，此节点管理此本地系统的所有内容
 *
 * @author 刘朕龙
 * @since 20201020
 *
 */
public interface KademliaNode
{

    /**
     * 开始定期刷新操作
     */
    public void startRefreshOperation();

    /**
     * 停止定期刷新操作
     */
    public void stopRefreshOperation();

    /**
     * @return Node 此系统的本地节点
     */
    public Node getNode();

    /**
     * @return KadServer用于发送/接收消息
     */
    public KadServer getServer();

    /**
     * @return 此kad实例的DHT
     */
    public KademliaDHT getDHT();

    /**
     * @return 当前使用的KadConfiguration对象
     */
    public KadConfiguration getCurrentConfiguration();

    /**
     * 连接到现有的对等网络
     *
     * @param n 对等网络中的已知节点
     *
     * @throws RoutingException      无法联系引导节点
     * @throws IOException           发生网络错误
     * @throws IllegalStateException 此对象关闭
     * */
    public void connect(Node n) throws IOException, RoutingException;

    /**
     * 在给定的 key 下存储指定的值
     * 该值存储在网络上的K个节点上，如果网络中的节点总数> K，则存储在所有节点上
     *
     * @param content 要放入DHT的内容
     *
     * @return Integer 内容存储在多少个节点上
     *
     * @throws java.io.IOException
     *
     */
    public int put(KadContent content) throws IOException;

    /**
     * 在给定的 key 下存储指定的值
     * 该值存储在网络上的K个节点上，如果网络中的节点总数> K，则存储在所有节点上
     *
     * @param entry 带有要放入DHT的内容的StorageEntry
     *
     * @return Integer 内容存储在多少个节点上
     *
     * @throws java.io.IOException
     *
     */
    public int put(KadStorageEntry entry) throws IOException;

    /**
     * 将内容存储在本地节点的DHT上
     *
     * @param content
     *
     * @throws java.io.IOException
     */
    public void putLocally(KadContent content) throws IOException;

    /**
     * 获取一些存储在DHT上的内容
     *
     * @param param 用于搜索内容的参数
     *
     * @return DHTContent
     *
     * @throws java.io.IOException
     * @throws org.kaddht.kademlia.exceptions.ContentNotFoundException
     */
    public KadStorageEntry get(GetParameter param) throws NoSuchElementException, IOException, ContentNotFoundException;

    /**
     *
     * @throws java.io.IOException
     */
    public void refresh() throws IOException;


    public String getOwnerId();

    public int getPort();


    public void shutdown(final boolean saveState) throws IOException;


    public void saveKadState() throws IOException;


    public KademliaRoutingTable getRoutingTable();


    public Statistician getStatistician();
}
