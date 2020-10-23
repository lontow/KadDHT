package org.kaddht.kademlia.dht;

import java.util.Objects;
import org.kaddht.kademlia.node.KademliaId;

/**
 * 跟踪DHT中存储的内容的数据
 *
 * @author Lontow
 * @since 20201020
 */
public class StorageEntryMetadata implements KademliaStorageEntryMetadata
{

    private final KademliaId key;
    private final String ownerId;
    private final String type;
    private final int contentHash;
    private final long updatedTs;

    private long lastRepublished;

    public StorageEntryMetadata(KadContent content)
    {
        this.key = content.getKey();
        this.ownerId = content.getOwnerId();
        this.type = content.getType();
        this.contentHash = content.hashCode();
        this.updatedTs = content.getLastUpdatedTimestamp();

        this.lastRepublished = System.currentTimeMillis() / 1000L;
    }

    public KademliaId getKey()
    {
        return this.key;
    }

    public String getOwnerId()
    {
        return this.ownerId;
    }


    public String getType()
    {
        return this.type;
    }


    public int getContentHash()
    {
        return this.contentHash;
    }

    public long getLastUpdatedTimestamp()
    {
        return this.updatedTs;
    }

    /**
     * 当节点在寻找内容时，将搜索条件封装在 GetParameter 对象中
     * 在这里，我们使用此GetParameter对象并检查此StorageEntry是否满足给定参数
     *
     * @param params
     *
     * @return boolean Whether this content satisfies the parameters
     */

    public boolean satisfiesParameters(GetParameter params)
    {
        //检查 ownerId 是否满足
        if ((params.getOwnerId() != null) && (!params.getOwnerId().equals(this.ownerId)))
        {
            return false;
        }

        //检查 type 是否满足
        if ((params.getType() != null) && (!params.getType().equals(this.type)))
        {
            return false;
        }

        //检查 key 是否满足
        if ((params.getKey() != null) && (!params.getKey().equals(this.key)))
        {
            return false;
        }

        return true;
    }

    @Override
    public long lastRepublished()
    {
        return this.lastRepublished;
    }

    /**
     * 当我们重新上传内容或从网络获取此内容时，应更新时间
     */

    public void updateLastRepublished()
    {
        this.lastRepublished = System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof KademliaStorageEntryMetadata)
        {
            return this.hashCode() == o.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.key);
        hash = 23 * hash + Objects.hashCode(this.ownerId);
        hash = 23 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("[StorageEntry: ");

        sb.append("{Key: ");
        sb.append(this.key);
        sb.append("} ");
        sb.append("{Owner: ");
        sb.append(this.ownerId);
        sb.append("} ");
        sb.append("{Type: ");
        sb.append(this.type);
        sb.append("} ");
        sb.append("]");

        return sb.toString();
    }
}
