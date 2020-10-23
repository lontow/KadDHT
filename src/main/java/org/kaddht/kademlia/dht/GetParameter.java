package org.kaddht.kademlia.dht;

import com.google.gson.annotations.SerializedName;
import org.kaddht.kademlia.node.KademliaId;

/**
 * GET请求可以基于键，所有者，类型等获取内容
 * 该类包含上述的在GET请求中需要传递的参数
 *
 * @author 刘朕龙
 * @since 20201018
 */
public class GetParameter
{
    @SerializedName("key")
    private KademliaId key;
    @SerializedName("type")
    private String type = null;
    @SerializedName("ownerId")
    private String ownerId = null;

    /**
     * 基于 NodeId 和 type 来寻找数据
     *
     * @param key
     * @param type
     */
    public GetParameter(KademliaId key, String type)
    {
        this.key = key;
        this.type = type;
    }

    /**
     * 基于 NodeId, owner, type 来寻找数据
     *
     * @param key
     * @param type
     * @param owner
     */
    public GetParameter(KademliaId key, String type, String owner)
    {
        this(key, type);
        this.ownerId = owner;
    }

    /**
     * 根据已有的内容来构建参数
     *
     * @param c
     */
    public GetParameter(KadContent c)
    {
        this.key = c.getKey();

        if (c.getType() != null)
        {
            this.type = c.getType();
        }

        if (c.getOwnerId() != null)
        {
            this.ownerId = c.getOwnerId();
        }
    }

    /**
     * 基于 StorageEntryMeta data 来构建参数
     *
     * @param md
     */
    public GetParameter(KademliaStorageEntryMetadata md)
    {
        this.key = md.getKey();

        if (md.getType() != null)
        {
            this.type = md.getType();
        }

        if (md.getOwnerId() != null)
        {
            this.ownerId = md.getOwnerId();
        }
    }

    public KademliaId getKey()
    {
        return this.key;
    }

    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    public String getOwnerId()
    {
        return this.ownerId;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    @Override
    public String toString()
    {
        return "GetParameter - [Key: " + key + "][Owner: " + this.ownerId + "][Type: " + this.type + "]";
    }
}
