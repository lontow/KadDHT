package org.kaddht.kademlia.simulations;

import java.io.IOException;
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
public class RefreshOperationTest
{

    public static void main(String[] args)
    {
        try
        {
            /* Setting up 2 Kad networks */
            JKademliaNode kad1 = new JKademliaNode("JoshuaK", new KademliaId("ASF45678947584567467"), 7574);
            JKademliaNode kad2 = new JKademliaNode("Crystal", new KademliaId("ASERTKJDHGVHERJHGFLK"), 7572);
            kad2.bootstrap(kad1.getNode());

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
