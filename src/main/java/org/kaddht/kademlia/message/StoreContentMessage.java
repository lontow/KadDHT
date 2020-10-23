package org.kaddht.kademlia.message;

import org.kaddht.kademlia.dht.KadStorageEntry;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.util.serializer.JsonSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Lontow
 * @since 20201020
 */
public class StoreContentMessage implements Message
{

    public static final byte CODE = 0x08;

    private KadStorageEntry content;
    private Node origin;

    /**
     * @param origin
     * @param content
     *
     */
    public StoreContentMessage(Node origin, KadStorageEntry content)
    {
        this.content = content;
        this.origin = origin;
    }

    public StoreContentMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.origin.toStream(out);

        /* Serialize the KadContent, then send it to the stream */
        new JsonSerializer<KadStorageEntry>().write(content, out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);
        try
        {
            this.content = new JsonSerializer<KadStorageEntry>().read(in);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    public KadStorageEntry getContent()
    {
        return this.content;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    @Override
    public String toString()
    {
        return "StoreContentMessage[origin=" + origin + ",content=" + content + "]";
    }
}
