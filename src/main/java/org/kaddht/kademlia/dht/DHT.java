package org.kaddht.kademlia.dht;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.exceptions.ContentExistException;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.util.serializer.JsonSerializer;
import org.kaddht.kademlia.util.serializer.KadSerializer;

/**
 * 管理整个DHT的主要 Distributed Hash Table类
 *
 * @author 刘朕龙
 * @create 2020-10-16
 */
public class DHT implements KademliaDHT
{

    private transient StoredContentManager contentManager;
    private transient KadSerializer<KadStorageEntry> serializer = null;
    private transient KadConfiguration config;

    private final String ownerId;

    public DHT(String ownerId, KadConfiguration config)
    {
        this.ownerId = ownerId;
        this.config = config;
        this.initialize();
    }

    @Override
    public final void initialize()
    {
        contentManager = new StoredContentManager();
    }

    @Override
    public void setConfiguration(KadConfiguration con)
    {
        this.config = con;
    }

    @Override
    public KadSerializer<KadStorageEntry> getSerializer()
    {
        if (null == serializer)
        {
            serializer = new JsonSerializer<>();
        }

        return serializer;
    }

    @Override
    public boolean store(KadStorageEntry content) throws IOException
    {
        // 检查一下是否有该内容，且是否为最新版本
        if (this.contentManager.contains(content.getContentMetadata()))
        {
            KademliaStorageEntryMetadata current = this.contentManager.get(content.getContentMetadata());

            // 更新最新提交的时间
            current.updateLastRepublished();

            if (current.getLastUpdatedTimestamp() >= content.getContentMetadata().getLastUpdatedTimestamp())
            {
                // 有该内容且为最新版本
                return false;
            }
            else
            {
                // 具有改内容但不是最新版本，将其删除并添加新版本
                try
                {
                    //System.out.println("Removing older content to update it");
                    this.remove(content.getContentMetadata());
                }
                catch (ContentNotFoundException ex)
                {
                    // 该异常不会发生，因为只有具有内容时才会到达此处
                }
            }
        }

        /**
         * 表明没有内容或者我们已经删除了它，下面将添加新内容
         */
        try
        {
            //System.out.println("Adding new content.");
            // 在条目管理器中跟踪改内容
            KademliaStorageEntryMetadata sEntry = this.contentManager.put(content.getContentMetadata());

            // 将内容存放在本地文件中
            String contentStorageFolder = this.getContentStorageFolderName(content.getContentMetadata().getKey());

            try (FileOutputStream fout = new FileOutputStream(contentStorageFolder + File.separator + sEntry.hashCode() + ".kct");
                    DataOutputStream dout = new DataOutputStream(fout))
            {
                this.getSerializer().write(content, dout);
            }
            return true;
        }
        catch (ContentExistException e)
        {
            /**
             * 理论上不应该发生，因为如果存在旧文件已删除，存在最新文件则方法早已返回
             */
            return false;
        }
    }

    @Override
    public boolean store(KadContent content) throws IOException
    {
        return this.store(new KadStorageEntry(content));
    }

    @Override
    public KadStorageEntry retrieve(KademliaId key, int hashCode) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        String folder = this.getContentStorageFolderName(key);
        DataInputStream din = new DataInputStream(new FileInputStream(folder + File.separator + hashCode + ".kct"));
        return this.getSerializer().read(din);
    }

    @Override
    public boolean contains(GetParameter param)
    {
        return this.contentManager.contains(param);
    }

    @Override
    public KadStorageEntry get(KademliaStorageEntryMetadata entry) throws IOException, NoSuchElementException
    {
        try
        {
            return this.retrieve(entry.getKey(), entry.hashCode());
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while loading file for content. Message: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("The class for some content was not found. Message: " + e.getMessage());
        }

        // 我们没有得到条目信息
        throw new NoSuchElementException();
    }

    @Override
    public KadStorageEntry get(GetParameter param) throws NoSuchElementException, IOException
    {
        // 条件成立则加载 KadContent
        try
        {
            KademliaStorageEntryMetadata e = this.contentManager.get(param);
            return this.retrieve(e.getKey(), e.hashCode());
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while loading file for content. Message: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("The class for some content was not found. Message: " + e.getMessage());
        }

        // 没有得到条目信息
        throw new NoSuchElementException();
    }

    @Override
    public void remove(KadContent content) throws ContentNotFoundException
    {
        this.remove(new StorageEntryMetadata(content));
    }

    @Override
    public void remove(KademliaStorageEntryMetadata entry) throws ContentNotFoundException
    {
        String folder = this.getContentStorageFolderName(entry.getKey());
        File file = new File(folder + File.separator + entry.hashCode() + ".kct");

        contentManager.remove(entry);

        if (file.exists())
        {
            file.delete();
        }
        else
        {
            throw new ContentNotFoundException();
        }
    }

    /**
     * 获取存储文件夹的名称
     *
     * @param key The key of the content
     *
     * @return String The name of the folder
     */
    private String getContentStorageFolderName(KademliaId key)
    {
        /**
         * 每个内容都存储在以NodeId的前2个字符命名的文件夹中
         *
         * 包含内容的文件的名称是该内容的哈希值
         */
        String folderName = key.hexRepresentation().substring(0, 2);
        File contentStorageFolder = new File(this.config.getNodeDataFolder(ownerId) + File.separator + folderName);

        // 如果文件夹不存在则创造它
        if (!contentStorageFolder.isDirectory())
        {
            contentStorageFolder.mkdir();
        }

        return contentStorageFolder.toString();
    }

    @Override
    public List<KademliaStorageEntryMetadata> getStorageEntries()
    {
        return contentManager.getAllEntries();
    }

    @Override
    public void putStorageEntries(List<KademliaStorageEntryMetadata> ientries)
    {
        for (KademliaStorageEntryMetadata e : ientries)
        {
            try
            {
                this.contentManager.put(e);
            }
            catch (ContentExistException ex)
            {
                // Entry 已存在
            }
        }
    }

    @Override
    public synchronized String toString()
    {
        return this.contentManager.toString();
    }
}
