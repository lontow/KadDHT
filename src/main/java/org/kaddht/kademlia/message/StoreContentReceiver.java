package org.kaddht.kademlia.message;

import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.dht.KademliaDHT;

import java.io.IOException;

/**
 *
 *
 * @author Lontow
 * @since 20201020
 */
public class StoreContentReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;

    public StoreContentReceiver(KadServer server, KademliaNode localNode, KademliaDHT dht)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
    }

    @Override
    public void receive(Message incoming, int comm)
    {
        StoreContentMessage msg = (StoreContentMessage) incoming;


        this.localNode.getRoutingTable().insert(msg.getOrigin());

        try
        {
            /* 保存内容 */
            this.dht.store(msg.getContent());
        }
        catch (IOException e)
        {
            System.err.println("Unable to store received content; Message: " + e.getMessage());
        }

    }

    @Override
    public void timeout(int comm)
    {
        /**
         *
         */
    }
}
