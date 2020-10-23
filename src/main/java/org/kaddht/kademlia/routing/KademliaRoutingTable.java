package org.kaddht.kademlia.routing;

import java.util.List;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.node.KademliaId;

/**
 * 路由表结构
 *
 * @author Lontow
 * @since 20201020
 */
public interface KademliaRoutingTable
{

    /**
     * 初始化
     */
    public void initialize();


    public void setConfiguration(KadConfiguration config);


    public void insert(Contact c);


    public void insert(Node n);

    /**
     * 计算　Node 应该放到那个　Bucket中
     */
    public int getBucketId(KademliaId nid);


    public List<Node> findClosest(KademliaId target, int numNodesRequired);


    public List getAllNodes();


    public List getAllContacts();


    public KademliaBucket[] getBuckets();

    /**
     * 将Contact 标记为无响应
     */
    public void setUnresponsiveContacts(List<Node> contacts);

    /**
     * 将　Node 标记为无响应
     *
     * @param n
     */
    public void setUnresponsiveContact(Node n);

}
