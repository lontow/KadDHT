package org.kaddht.kademlia.message;

import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;

import java.io.IOException;

/**
 * 连接和确认
 *
 * @author Lontow
 * @created 20201015
 */
public class ConnectReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;

    public ConnectReceiver(KadServer server, KademliaNode local)
    {
        this.server = server;
        this.localNode = local;
    }

    /**
     * Handle receiving a ConnectMessage
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        ConnectMessage mess = (ConnectMessage) incoming;

        /* Update the local space by inserting the origin node. */
        this.localNode.getRoutingTable().insert(mess.getOrigin());

        /* Respond to the connect request */
        AcknowledgeMessage msg = new AcknowledgeMessage(this.localNode.getNode());

        /* Reply to the connect message with an Acknowledgement */
        this.server.reply(mess.getOrigin(), msg, comm);
    }

    /**
     * We don't need to do anything here
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public void timeout(int comm) throws IOException
    {
    }
}
