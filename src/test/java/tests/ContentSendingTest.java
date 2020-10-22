package tests;

import java.io.IOException;
import java.util.UUID;

import org.kaddht.kademlia.dht.DHTContentImpl;
import org.kaddht.kademlia.dht.GetParameter;
import org.kaddht.kademlia.JKademliaNode;
import org.kaddht.kademlia.dht.KademliaStorageEntry;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.node.KademliaId;

/**
 * Testing sending and receiving content between 2 Nodes on a network
 *
 * @author Lontow
 * @since 20201020
 */
public class ContentSendingTest
{

    public static void main(String[] args)
    {
        try
        {
            /* Setting up 2 Kad networks */
            JKademliaNode kad1 = new JKademliaNode("JoshuaK", new KademliaId("ASF45678947584567467"), 7574);
            System.out.println("Created Node Kad 1: " + kad1.getNode().getNodeId());
            JKademliaNode kad2 = new JKademliaNode("Crystal", new KademliaId("ASERTKJDHGVHERJHGFLK"), 7572);
            System.out.println("Created Node Kad 2: " + kad2.getNode().getNodeId());
            JKademliaNode kad3 = new JKademliaNode("Shameer", new KademliaId("AS84k67894758456746A"), 8104);
            kad2.bootstrap(kad1.getNode());
            System.out.println("Created Node Kad 3: " + kad3.getNode().getNodeId());
            kad3.bootstrap(kad1.getNode());

            /**
             * Lets create the content and share it
             */
            String data = "";
            for (int i = 0; i < 500; i++)
            {
                data += UUID.randomUUID();
            }
            System.out.println(data);
            DHTContentImpl c = new DHTContentImpl(new KademliaId("ASERTKJDHGVHERJHGFLE"),kad2.getOwnerId(), data);
            kad2.put(c);


            kad2.shutdown(true);
            kad1.shutdown(true);
            kad1 = JKademliaNode.loadFromFile("JoshuaK");
            kad1.bootstrap(kad3.getNode());
            /**
             * Lets retrieve the content
             */
            System.out.println("Retrieving Content");
            GetParameter gp = new GetParameter(c.getKey(), DHTContentImpl.TYPE);
            gp.setOwnerId(c.getOwnerId());
            System.out.println("Get Parameter: " + gp);
            KademliaStorageEntry conte = kad3.get("eyJrZXkiOnsia2V5Qnl0ZXMiOls2NSw4Myw2OSw4Miw4NCw3NSw3NCw2OCw3Miw3MSw4Niw3Miw2OSw4Miw3NCw3Miw3MSw3MCw3Niw2OV19LCJ0eXBlIjoiREhUQ29udGVudEltcGwiLCJvd25lcklkIjoiQ3J5c3RhbCJ9");
            System.out.println("Content Found: " + new DHTContentImpl().fromSerializedForm(conte.getContent()));
            System.out.println("Content Metadata: " + conte.getContentMetadata());

        }
        catch (IOException | ContentNotFoundException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
