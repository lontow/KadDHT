package org.kaddht.kademlia.dht;

import org.kaddht.kademlia.node.KademliaId;

/**
 * 任何需要存储在 DHT 上的内容
 *
 * @author 张文令
 *
 * @since 20201012
 */
public interface KadContent
{

    public KademliaId getKey();

    public String getType();

    public long getCreatedTimestamp();

    public long getLastUpdatedTimestamp();

    public String getOwnerId();

    /**
     * 将内容进行序列化，只有序列化后的对象才能在网络传输
     *
     * @return 序列化后的对象
     */
    public byte[] toSerializedForm();

    /**
     * 进行反序列化
     *
     * @param data The object in byte format
     *
     * @return A new object from the given
     */
    public KadContent fromSerializedForm(byte[] data);
}
