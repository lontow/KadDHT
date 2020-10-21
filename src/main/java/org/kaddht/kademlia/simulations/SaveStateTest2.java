package org.kaddht.kademlia.simulations;

import org.kaddht.kademlia.JKademliaNode;
import org.kaddht.kademlia.dht.GetParameter;
import org.kaddht.kademlia.dht.KademliaStorageEntry;
import org.kaddht.kademlia.node.KademliaId;

/**
 * Testing the save and retrieve state operations.
 * Here we also try to look for content on a restored node
 *
 * @author Lontow
 * @since 20201020
 */
public class SaveStateTest2
{

    public SaveStateTest2()
    {
        try
        {
            /* Setting up 2 Kad networks */
            JKademliaNode kad1 = new JKademliaNode("JoshuaK", new KademliaId("ASF45678947584567463"), 12049);
            JKademliaNode kad2 = new JKademliaNode("Crystal", new KademliaId("ASF45678947584567464"), 4585);

            /* Connecting 2 to 1 */
            System.out.println("Connecting Nodes 1 & 2");
            kad2.bootstrap(kad1.getNode());
            //System.out.println(kad1);
            //System.out.println(kad2);

            DHTContentImpl c;
            synchronized (this)
            {
                System.out.println("\n\n\n\nSTORING CONTENT 1\n\n\n\n");
                c = new DHTContentImpl(kad2.getOwnerId(), "Some Data");
                System.out.println(c);
                kad1.putLocally(c);
                //kad1.put(c);
            }

           // System.out.println(kad1);
            //System.out.println(kad2);

            /* Shutting down kad1 and restarting it */
            System.out.println("\n\n\nShutting down Kad 1 instance");
            kad1.shutdown(true);

            System.out.println("\n\n\nReloading Kad instance from file");
            kad1 = JKademliaNode.loadFromFile("JoshuaK");
            kad1.bootstrap(kad2.getNode());
            System.out.println(kad2);

            /* Trying to get a content stored on the restored node */
            GetParameter gp = new GetParameter(c.getKey(),c.getType(), kad2.getOwnerId());
            System.out.println(gp);

            KademliaStorageEntry content = kad1.get(gp);

            DHTContentImpl cc = new DHTContentImpl().fromSerializedForm(content.getContent());
            System.out.println("Content received: " + cc);

            JKademliaNode kad3 = new JKademliaNode("Shameer", new KademliaId("AS84k67894758456746A"), 8104);
            System.out.println("Created Node Kad 3: " + kad3.getNode().getNodeId());
            kad3.bootstrap(kad1.getNode());
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        new SaveStateTest2();
    }
}
