package org.kaddht.kademlia.dht;

import org.kaddht.kademlia.node.KademliaId;

/**
 * 跟踪 DHT 中存储的内容的数据
 * 便于StorageEntryManager类使用
 *
 * @author 张文令
 * @since 20201012
 */
public interface KademliaStorageEntryMetadata
{

    public KademliaId getKey();

    public String getOwnerId();

    public String getType();


    public int getContentHash();


    public long getLastUpdatedTimestamp();

    /**
     * 当节点正在寻找内容时，他在 GetParameter 对象中发送搜索条件
     * 在这里，我们使用此GetParameter对象并检查此StorageEntry是否满足给定的参数
     *
     * @param params
     *
     * @return boolean Whether this content satisfies the parameters
     */
    public boolean satisfiesParameters(GetParameter params);

    /**
     * @return 内容最后被更新的时间
     */
    public long lastRepublished();

    /**
     * 当重新上传内容或从网络获取此内容时，应更新时间
     */
    public void updateLastRepublished();
}
