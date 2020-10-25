package org.kaddht.kademlia.message;

import java.io.IOException;

/**
 *默认　Receiver
 *
 * @author Lontow
 * @created 20201015
 */
public class SimpleReceiver implements Receiver
{

    @Override
    public void receive(Message incoming, int conversationId)
    {
        //System.out.println("Received message: " + incoming);
    }

    @Override
    public void timeout(int conversationId) throws IOException
    {
        //System.out.println("SimpleReceiver message timeout.");
    }
}
