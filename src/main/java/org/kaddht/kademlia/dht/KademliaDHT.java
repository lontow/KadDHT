package org.kaddht.kademlia.dht;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.util.serializer.KadSerializer;

/**
 * 管理整个DHT的主要 Distributed Hash Table 的接口
 * The main Distributed Hash Table interface that manages the entire DHT
 *
 * @author Lontow
 * @since 20201020
 */
public interface KademliaDHT
{

    /**
     * 初始化分布式哈希表
     */
    public void initialize();

    /**
     * 设置配置文件
     *
     * @param con The new configuration file
     */
    public void setConfiguration(KadConfiguration con);

    /**
     * 创建一个新的序列化器或返回一个现有的序列化器
     *
     * @return The new ContentSerializer
     */
    public KadSerializer<KadStorageEntry> getSerializer();

    /**
     * 处理本地存储内容
     *
     * @param content The DHT content to store
     *
     * @return 如果我们存储了新内容返回 true，否则返回 false
     *
     * @throws java.io.IOException
     */
    public boolean store(KadStorageEntry content) throws IOException;

    public boolean store(KadContent content) throws IOException;

    /**
     * 从本地存储中检索内容
     *
     * @param key      The Key of the content to retrieve
     * @param hashCode The hash code of the content to retrieve
     *
     * @return A KadContent object
     *
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public KadStorageEntry retrieve(KademliaId key, int hashCode) throws FileNotFoundException, IOException, ClassNotFoundException;

    /**
     * 检查此DHT中是否存在给定条件的任何内容
     *
     * @param param 内容搜索条件
     *
     * @return 返回是否有满足搜索条件的内容
     */
    public boolean contains(GetParameter param);

    /**
     * 对于的给定 StorageEntry 对象，检索并创建一个KadContent对象
     *
     * @param entry 用于检索此内容的StorageEntry
     *
     * @return KadContent The content object
     *
     * @throws java.io.IOException
     */
    public KadStorageEntry get(KademliaStorageEntryMetadata entry) throws IOException, NoSuchElementException;

    /**
     * 内容如果存在，返回内容的StorageEntry
     *
     * @param param
     *
     * @return 在DHT上找到的满足给定条件的KadContent
     *
     * @throws java.io.IOException
     */
    public KadStorageEntry get(GetParameter param) throws NoSuchElementException, IOException;

    /**
     * 从本地存储中删除一个内容
     *
     * @param content 需要删除的内容
     *
     *
     * @throws org.kaddht.kademlia.exceptions.ContentNotFoundException
     */
    public void remove(KadContent content) throws ContentNotFoundException;

    public void remove(KademliaStorageEntryMetadata entry) throws ContentNotFoundException;

    /**
     * @return 此节点的所有StorageEntries的列表
     */
    public List<KademliaStorageEntryMetadata> getStorageEntries();

    /**
     * 用于将现有内容的存储条目列表添加到 DHT
     * 主要在从已保存的状态文件中检索 StorageEntries 时使用
     *
     * @param ientries The entries to add
     */
    public void putStorageEntries(List<KademliaStorageEntryMetadata> ientries);

}
