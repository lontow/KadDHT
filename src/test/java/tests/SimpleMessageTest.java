package tests;

import java.io.IOException;
import org.kaddht.kademlia.JKademliaNode;
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
            JKademliaNode kad1 = new JKademliaNode("Joshua", new KademliaId("12345678901234567890"), 7574);
            JKademliaNode kad2 = new JKademliaNode("Crystal", new KademliaId("12345678901234567891"), 7572);

            kad1.getServer().sendMessage(kad2.getNode(), new SimpleMessage("Some Message"), new SimpleReceiver());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
