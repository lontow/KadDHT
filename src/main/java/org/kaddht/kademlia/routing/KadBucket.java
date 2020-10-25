package org.kaddht.kademlia.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.node.Node;

/**
 *  路由表中　Bucket
 *
 * @author Lontow
 * @created 20201023
 */
public class KadBucket implements KademliaBucket
{

    /* 深度 */
    private final int depth;

    /* 存储 */
    private final TreeSet<Contact> contacts;

    /* 缓存 */
    private final TreeSet<Contact> replacementCache;

    private final KadConfiguration config;

    
    {
        contacts = new TreeSet<>();
        replacementCache = new TreeSet<>();
    }

    /**
     * @param depth
     * @param config
     */
    public KadBucket(int depth, KadConfiguration config)
    {
        this.depth = depth;
        this.config = config;
    }

    @Override
    public synchronized void insert(Contact c)
    {
        if (this.contacts.contains(c))
        {
            /**
             * 删除后，更新时间再重新加入。触发排序
             */
            Contact tmp = this.removeFromContacts(c.getNode());
            tmp.setSeenNow();
            tmp.resetStaleCount();
            this.contacts.add(tmp);
        }
        else
        {
            /* 若　Bucket 满了，放入缓存 */
            if (contacts.size() >= this.config.k())
            {
                /* 查看过期时间最长的 */
                Contact stalest = null;
                for (Contact tmp : this.contacts)
                {
                    if (tmp.staleCount() >= this.config.stale())
                    {
                        /* Contact 过期 */
                        if (stalest == null)
                        {
                            stalest = tmp;
                        }
                        else if (tmp.staleCount() > stalest.staleCount())
                        {
                            stalest = tmp;
                        }
                    }
                }

                /* 替换过期的 */
                if (stalest != null)
                {
                    this.contacts.remove(stalest);
                    this.contacts.add(c);
                }
                else
                {
                    /* 没过期的，加入缓存 */
                    this.insertIntoReplacementCache(c);
                }
            }
            else
            {
                this.contacts.add(c);
            }
        }
    }

    @Override
    public synchronized void insert(Node n)
    {
        this.insert(new Contact(n));
    }

    @Override
    public synchronized boolean containsContact(Contact c)
    {
        return this.contacts.contains(c);
    }

    @Override
    public synchronized boolean containsNode(Node n)
    {
        return this.containsContact(new Contact(n));
    }

    @Override
    public synchronized boolean removeContact(Contact c)
    {
        if (!this.contacts.contains(c))
        {
            return false;
        }

        /* 缓存中有，删除 */
        if (!this.replacementCache.isEmpty())
        {
            this.contacts.remove(c);
            Contact replacement = this.replacementCache.first();
            this.contacts.add(replacement);
            this.replacementCache.remove(replacement);
        }
        else
        {
            /* 缓存中无，记录过期 */
            this.getFromContacts(c.getNode()).incrementStaleCount();
        }

        return true;
    }

    private synchronized Contact getFromContacts(Node n)
    {
        for (Contact c : this.contacts)
        {
            if (c.getNode().equals(n))
            {
                return c;
            }
        }

        /* This contact does not exist */
        throw new NoSuchElementException("The contact does not exist in the contacts list.");
    }

    private synchronized Contact removeFromContacts(Node n)
    {
        for (Contact c : this.contacts)
        {
            if (c.getNode().equals(n))
            {
                this.contacts.remove(c);
                return c;
            }
        }

        /* We got here means this element does not exist */
        throw new NoSuchElementException("Node does not exist in the replacement cache. ");
    }

    @Override
    public synchronized boolean removeNode(Node n)
    {
        return this.removeContact(new Contact(n));
    }

    @Override
    public synchronized int numContacts()
    {
        return this.contacts.size();
    }

    @Override
    public synchronized int getDepth()
    {
        return this.depth;
    }

    @Override
    public synchronized List<Contact> getContacts()
    {
        final ArrayList<Contact> ret = new ArrayList<>();

        /* If we have no contacts, return the blank arraylist */
        if (this.contacts.isEmpty())
        {
            return ret;
        }

        /* We have contacts, lets copy put them into the arraylist and return */
        for (Contact c : this.contacts)
        {
            ret.add(c);
        }

        return ret;
    }

    /**
     * When the bucket is filled, we keep extra contacts in the replacement cache.
     */
    private synchronized void insertIntoReplacementCache(Contact c)
    {
        /* Just return if this contact is already in our replacement cache */
        if (this.replacementCache.contains(c))
        {
            /**
             * If the contact is already in the bucket, lets update that we've seen it
             * We need to remove and re-add the contact to get the Sorted Set to update sort order
             */
            Contact tmp = this.removeFromReplacementCache(c.getNode());
            tmp.setSeenNow();
            this.replacementCache.add(tmp);
        }
        else if (this.replacementCache.size() > this.config.k())
        {
            /* if our cache is filled, we remove the least recently seen contact */
            this.replacementCache.remove(this.replacementCache.last());
            this.replacementCache.add(c);
        }
        else
        {
            this.replacementCache.add(c);
        }
    }

    private synchronized Contact removeFromReplacementCache(Node n)
    {
        for (Contact c : this.replacementCache)
        {
            if (c.getNode().equals(n))
            {
                this.replacementCache.remove(c);
                return c;
            }
        }

        /* We got here means this element does not exist */
        throw new NoSuchElementException("Node does not exist in the replacement cache. ");
    }

    @Override
    public synchronized String toString()
    {
        StringBuilder sb = new StringBuilder("Bucket at depth: ");
        sb.append(this.depth);
        int i = 0;
        sb.append("\nNodes:                    NodeId                  stales\n");
        for (Contact n : this.contacts)
        {
            sb.append("Node");
            sb.append(++i);
            sb.append(":  ");
            sb.append(n.getNode().getNodeId().toString());
//            sb.append(" (stale: ");
            sb.append("    ");
            sb.append(n.staleCount());
//            sb.append(")");
            sb.append("\n");
        }

        return sb.toString();
    }
}
