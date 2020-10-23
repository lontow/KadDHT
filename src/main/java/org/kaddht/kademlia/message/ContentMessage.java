package org.kaddht.kademlia.message;

import org.kaddht.kademlia.dht.KadStorageEntry;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.util.serializer.JsonSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A Message used to send content between nodes
 *
 * @author Lontow
 * @since 20201020
 */
public class ContentMessage implements Message
{

    public static final byte CODE = 0x04;

    private KadStorageEntry content;
    private Node origin;

    /**
     * @param origin  Where the message came from
     * @param content The content to be stored
     *
     */
    public ContentMessage(Node origin, KadStorageEntry content)
    {
        this.content = content;
        this.origin = origin;
    }

    public ContentMessage(DataInputStream in) throws IOException
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
            System.err.println("ClassNotFoundException when reading StorageEntry; Message: " + e.getMessage());
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
        return "ContentMessage[origin=" + origin + ",content=" + content + "]";
    }
}
