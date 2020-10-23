package org.kaddht.kademlia.routing;

import org.kaddht.kademlia.node.Node;

/**
 * 路由项信息
 * Node 之上的封装，实际保存在 Bucket
 *
 *
 * @author Lontow
 * @since 20201020
 * @updated 20201024
 */
public class Contact implements Comparable<Contact>
{

    private final Node n;
    private long lastSeen;

    /**
     * 如果路由项为空，缓存为空，或者该路由项无法被替换，则标记为　stale
     */
    private int staleCount;

    /**
     *
     *
     * @param n Node
     */
    public Contact(Node n)
    {
        this.n = n;
        this.lastSeen = System.currentTimeMillis() / 1000L;
    }

    public Node getNode()
    {
        return this.n;
    }

    /**
     * 更新时间戳为当前时间
     */
    public void setSeenNow()
    {
        this.lastSeen = System.currentTimeMillis() / 1000L;
    }

    /**
     * 返回上一次被标记的时间
     *
     * @return
     */
    public long lastSeen()
    {
        return this.lastSeen;
    }

    @Override
    public boolean equals(Object c)
    {
        if (c instanceof Contact)
        {
            return ((Contact) c).getNode().equals(this.getNode());
        }

        return false;
    }

    /**
     * 记录 Contact 不能正常　respond 的次数
     */
    public void incrementStaleCount()
    {
        staleCount++;
    }


    public int staleCount()
    {
        return this.staleCount;
    }

    /**
     * Contact 再次加入
     */
    public void resetStaleCount()
    {
        this.staleCount = 0;
    }

    @Override
    public int compareTo(Contact o)
    {
        if (this.getNode().equals(o.getNode()))
        {
            return 0;
        }

        return (this.lastSeen() > o.lastSeen()) ? 1 : -1;
    }

    @Override
    public int hashCode()
    {
        return this.getNode().hashCode();
    }

}
