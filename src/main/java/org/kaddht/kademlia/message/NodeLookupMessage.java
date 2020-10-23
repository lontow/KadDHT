package org.kaddht.kademlia.message;

import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *查询到某个 key 最近的 K个 Node
 *
 * @author Lontow
 * @created 20201020
 */
public class NodeLookupMessage implements Message
{

    private Node origin;
    private KademliaId lookupId;

    public static final byte CODE = 0x05;

    /**
     * 构造函数
     *
     * @param origin 消息发送者
     * @param lookup 要查询　key
     */
    public NodeLookupMessage(Node origin, KademliaId lookup)
    {
        this.origin = origin;
        this.lookupId = lookup;
    }

    public NodeLookupMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);
        this.lookupId = new KademliaId(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.origin.toStream(out);
        this.lookupId.toStream(out);
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    public KademliaId getLookupId()
    {
        return this.lookupId;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    @Override
    public String toString()
    {
        return "NodeLookupMessage[origin=" + origin + ",lookup=" + lookupId + "]";
    }
}
