package org.kaddht.kademlia.message;

import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.dht.KademliaDHT;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 消息工厂的实现
 *
 * @author Lontow
 * @since 20201020
 */
public class MessageFactory implements KademliaMessageFactory
{

    private final KademliaNode localNode;
    private final KademliaDHT dht;
    private final KadConfiguration config;

    public MessageFactory(KademliaNode local, KademliaDHT dht, KadConfiguration config)
    {
        this.localNode = local;
        this.dht = dht;
        this.config = config;
    }

    @Override
    public Message createMessage(byte code, DataInputStream in) throws IOException
    {
        switch (code)
        {
            case AcknowledgeMessage.CODE:
                return new AcknowledgeMessage(in);
            case ConnectMessage.CODE:
                return new ConnectMessage(in);
            case ContentMessage.CODE:
                return new ContentMessage(in);
            case ContentLookupMessage.CODE:
                return new ContentLookupMessage(in);
            case NodeLookupMessage.CODE:
                return new NodeLookupMessage(in);
            case NodeReplyMessage.CODE:
                return (Message) new NodeReplyMessage(in);
            case SimpleMessage.CODE:
                return new SimpleMessage(in);
            case StoreContentMessage.CODE:
                return new StoreContentMessage(in);
            default:
                //System.out.println(this.localNode + " - No Message handler found for message. Code: " + code);
                return new SimpleMessage(in);

        }
    }

    @Override
    public Receiver createReceiver(byte code, KadServer server)
    {
        switch (code)
        {
            case ConnectMessage.CODE:
                return new ConnectReceiver(server, this.localNode);
            case ContentLookupMessage.CODE:
                return new ContentLookupReceiver(server, this.localNode, this.dht, this.config);
            case NodeLookupMessage.CODE:
                return new NodeLookupReceiver(server, this.localNode, this.config);
            case StoreContentMessage.CODE:
                return new StoreContentReceiver(server, this.localNode, this.dht);
            default:
                //System.out.println("No receiver found for message. Code: " + code);
                return new SimpleReceiver();
        }
    }
}
