package tests;

import java.io.IOException;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.message.SimpleMessage;
import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.message.SimpleReceiver;

/**
 * Test 1: Try sending a simple message between nodes
 *
 * @author Lontow
 * @created 20201020
 */
public class SimpleMessageTest
{

    public static void main(String[] args)
    {
        try
        {
            KadPeer kad1 = new KadPeer("Joshua", new KademliaId("12345678901234567890"), 7574);
            KadPeer kad2 = new KadPeer("Crystal", new KademliaId("12345678901234567891"), 7572);

            kad1.getServer().sendMessage(kad2.getNode(), new SimpleMessage("Some Message"), new SimpleReceiver());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
