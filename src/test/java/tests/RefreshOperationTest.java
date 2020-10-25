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
public class RefreshOperationTest
{

    public static void main(String[] args)
    {
        try
        {
            /* Setting up 2 Kad networks */
            KadPeer kad1 = new KadPeer("JoshuaK", new KademliaId("ASF45678947584567467"), 7574);
            KadPeer kad2 = new KadPeer("Crystal", new KademliaId("ASERTKJDHGVHERJHGFLK"), 7572);
            kad2.connect(kad1.getNode());

            /* Lets create the content and share it */
            DHTContentImpl c = new DHTContentImpl(kad2.getOwnerId(), "Some Data");
            kad2.put(c);

            /* Lets retrieve the content */
            GetParameter gp = new GetParameter(c.getKey(), DHTContentImpl.TYPE);
            gp.setType(DHTContentImpl.TYPE);
            gp.setOwnerId(c.getOwnerId());
            KademliaStorageEntry conte = kad2.get(gp);

            kad2.refresh();
        }
        catch (IOException | ContentNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
