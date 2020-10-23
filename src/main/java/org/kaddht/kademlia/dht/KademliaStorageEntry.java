package org.kaddht.kademlia.dht;

/**
 * 用于在DHT上存储内容的存储条目类的StorageEntry接口
 *
 * @author 张文令
 * @since 20201023
 */
public interface KademliaStorageEntry
{

    /**
     * 将内容添加到存储条目
     *
     * @param data
     */
    public void setContent(final byte[] data);

    /**
     * 从此存储条目获取内容
     *
     * @return The content in byte format
     */
    public byte[] getContent();

    /**
     * 获取此存储条目的元数据
     *
     * @return the storage entry metadata
     */
    public KademliaStorageEntryMetadata getContentMetadata();
}
