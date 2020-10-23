package tests;

import java.io.IOException;

import org.kaddht.kademlia.dht.DHTContentImpl;
import org.kaddht.kademlia.dht.GetParameter;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.dht.KademliaStorageEntry;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.node.KademliaId;

/**
 * Testing sending and receiving content between 2 Nodes on a network
 *
 * @author Lontow
 * @since 20201020
 */
public class ContentUpdatingTest
{

    public static void main(String[] args)
    {
        try
        {
            /* Setting up 2 Kad networks */
            KadPeer kad1 = new KadPeer("JoshuaK", new KademliaId("ASF45678947584567467"), 7574);
            System.out.println("Created Node Kad 1: " + kad1.getNode().getNodeId());
            KadPeer kad2 = new KadPeer("Crystal", new KademliaId("ASERTKJDHGVHERJHGFLK"), 7572);
            System.out.println("Created Node Kad 2: " + kad2.getNode().getNodeId());
            kad2.bootstrap(kad1.getNode());

            /* Lets create the content and share it */
            DHTContentImpl c = new DHTContentImpl(kad2.getOwnerId(), "Some Data");
            kad2.put(c);

            /* Lets retrieve the content */
            System.out.println("Retrieving Content");
            GetParameter gp = new GetParameter(c.getKey(), DHTContentImpl.TYPE, c.getOwnerId());
            
            System.out.println("Get Parameter: " + gp);
            KademliaStorageEntry conte = kad2.get(gp);
            System.out.println("Content Found: " + new DHTContentImpl().fromSerializedForm(conte.getContent()));
            System.out.println("Content Metadata: " + conte.getContentMetadata());

            /* Lets update the content and put it again */
            c.setData("Some New Data");
            kad2.put(c);

            /* Lets retrieve the content */
            System.out.println("Retrieving Content Again");
            conte = kad2.get(gp);
            System.out.println("Content Found: " + new DHTContentImpl().fromSerializedForm(conte.getContent()));
            System.out.println("Content Metadata: " + conte.getContentMetadata());

        }
        catch (IOException | ContentNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
