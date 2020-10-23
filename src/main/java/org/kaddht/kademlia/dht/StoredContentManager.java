package org.kaddht.kademlia.dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.kaddht.kademlia.exceptions.ContentExistException;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.node.KademliaId;

/**
 * 将内容存储在本地文件中，用该类跟踪存储的所有内容
 *
 * @author 张文令
 * @since 20201018
 */
class StoredContentManager
{

    private final Map<KademliaId, List<KademliaStorageEntryMetadata>> entries;

    
    {
        entries = new HashMap<KademliaId, List<KademliaStorageEntryMetadata>>();
    }

    /**
     * 在存储中加一个新条目
     *
     * @param content
     */
    public KademliaStorageEntryMetadata put(KadContent content) throws ContentExistException
    {
        return this.put(new StorageEntryMetadata(content));
    }

    /**
     * 在存储中加一个新条目
     *
     * @param entry The StorageEntry to store
     */
    public KademliaStorageEntryMetadata put(KademliaStorageEntryMetadata entry) throws ContentExistException
    {
        if (!this.entries.containsKey(entry.getKey()))
        {
            this.entries.put(entry.getKey(), new ArrayList<KademliaStorageEntryMetadata>());
        }
        // 如果条目不存在，则添加它
        if (!this.contains(entry))
        {
            this.entries.get(entry.getKey()).add(entry);

            return entry;
        }
        else
        {
            throw new ContentExistException("Content already exists on this DHT");
        }
    }

    /**
     * 检查我们的DHT是否具有给定条件的内容
     *
     * @param param 搜索条件
     *
     * @return boolean
     */
    public synchronized boolean contains(GetParameter param)
    {
        if (this.entries.containsKey(param.getKey()))
        {
            // 检查剩余条件是否匹配
            for (KademliaStorageEntryMetadata e : this.entries.get(param.getKey()))
            {
                // 如果所有条件都匹配
                if (e.satisfiesParameters(param))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查DHT中是否存在该内容
     */
    public synchronized boolean contains(KadContent content)
    {
        return this.contains(new GetParameter(content));
    }

    /**
     * 检查此DHT上是否存在StorageEntry
     */
    public synchronized boolean contains(KademliaStorageEntryMetadata entry)
    {
        return this.contains(new GetParameter(entry));
    }

    /**
     * 检查我们的DHT是否具有给定条件的内容
     *
     * @param param 用于搜索内容的参数
     *
     * @return 特定搜索参数的内容列表
     */
    public KademliaStorageEntryMetadata get(GetParameter param) throws NoSuchElementException
    {
        if (this.entries.containsKey(param.getKey()))
        {
            // 存在带有此关键字的内容，检查剩余搜索条件是否匹配
            for (KademliaStorageEntryMetadata e : this.entries.get(param.getKey()))
            {
                // 如果存在条目满足给定参数，则返回true
                if (e.satisfiesParameters(param))
                {
                    return e;
                }
            }

            throw new NoSuchElementException();
        }
        else
        {
            throw new NoSuchElementException("No content exist for the given parameters");
        }
    }

    public KademliaStorageEntryMetadata get(KademliaStorageEntryMetadata md)
    {
        return this.get(new GetParameter(md));
    }

    /**
     * @return 所有存储条目的列表
     */
    public synchronized List<KademliaStorageEntryMetadata> getAllEntries()
    {
        List<KademliaStorageEntryMetadata> entriesRet = new ArrayList<>();

        for (List<KademliaStorageEntryMetadata> entrySet : this.entries.values())
        {
            if (entrySet.size() > 0)
            {
                entriesRet.addAll(entrySet);
            }
        }

        return entriesRet;
    }

    public void remove(KadContent content) throws ContentNotFoundException
    {
        this.remove(new StorageEntryMetadata(content));
    }

    public void remove(KademliaStorageEntryMetadata entry) throws ContentNotFoundException
    {
        if (contains(entry))
        {
            this.entries.get(entry.getKey()).remove(entry);
        }
        else
        {
            throw new ContentNotFoundException("This content does not exist in the Storage Entries");
        }
    }

    @Override
    public synchronized String toString()
    {
        StringBuilder sb = new StringBuilder("Stored Content: \n");
        int count = 0;
        for (List<KademliaStorageEntryMetadata> es : this.entries.values())
        {
            if (entries.size() < 1)
            {
                continue;
            }

            for (KademliaStorageEntryMetadata e : es)
            {
                sb.append(++count);
                sb.append(". ");
                sb.append(e);
                sb.append("\n");
            }
        }

        sb.append("\n");
        return sb.toString();
    }
}
