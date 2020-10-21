package org.kaddht.kademlia.message;

import org.kaddht.kademlia.dht.JKademliaStorageEntry;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.util.serializer.JsonSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A StoreContentMessage used to send a store message to a node
 *
 * @author Lontow
 * @since 20201020
 */
public class StoreContentMessage implements Message
{

    public static final byte CODE = 0x08;

    private JKademliaStorageEntry content;
    private Node origin;

    /**
     * @param origin  Where the message came from
     * @param content The content to be stored
     *
     */
    public StoreContentMessage(Node origin, JKademliaStorageEntry content)
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
        new JsonSerializer<JKademliaStorageEntry>().write(content, out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);
        try
        {
            this.content = new JsonSerializer<JKademliaStorageEntry>().read(in);
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

    public JKademliaStorageEntry getContent()
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
