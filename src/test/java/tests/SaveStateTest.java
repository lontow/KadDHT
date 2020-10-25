package tests;

import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.dht.DHTContentImpl;
import org.kaddht.kademlia.node.KademliaId;

/**
 * Testing the save and retrieve state operations
 *
 * @author Lontow
 * @since 20201020
 */
public class SaveStateTest
{

    public SaveStateTest()
    {
        try
        {
            /* Setting up 2 Kad networks */
            KadPeer kad1 = new KadPeer("JoshuaK", new KademliaId("ASF45678947584567463"), 12049);
            KadPeer kad2 = new KadPeer("Crystal", new KademliaId("ASF45678947584567464"), 4585);
            KadPeer kad3 = new KadPeer("Shameer", new KademliaId("ASF45678947584567465"), 8104);
            KadPeer kad4 = new KadPeer("Lokesh", new KademliaId("ASF45678947584567466"), 8335);
            KadPeer kad5 = new KadPeer("Chandu", new KademliaId("ASF45678947584567467"), 13345);

            /* Connecting 2 to 1 */
            System.out.println("Connecting Nodes 1 & 2");
            kad2.connect(kad1.getNode());
            System.out.println(kad1);
            System.out.println(kad2);

            kad3.connect(kad2.getNode());
            System.out.println(kad1);
            System.out.println(kad2);
            System.out.println(kad3);

            kad4.connect(kad2.getNode());
            System.out.println(kad1);
            System.out.println(kad2);
            System.out.println(kad3);
            System.out.println(kad4);

            kad5.connect(kad4.getNode());

            System.out.println(kad1);
            System.out.println(kad2);
            System.out.println(kad3);
            System.out.println(kad4);
            System.out.println(kad5);

            synchronized (this)
            {
                System.out.println("\n\n\n\nSTORING CONTENT 1\n\n\n\n");
                DHTContentImpl c = new DHTContentImpl(kad2.getOwnerId(), "Some Data");
                System.out.println(c);
                kad2.put(c);
            }

            synchronized (this)
            {
                System.out.println("\n\n\n\nSTORING CONTENT 2\n\n\n\n");
                DHTContentImpl c2 = new DHTContentImpl(kad2.getOwnerId(), "Some other Data");
                System.out.println(c2);
                kad4.put(c2);
            }

            System.out.println(kad1);
            System.out.println(kad2);
            System.out.println(kad3);
            System.out.println(kad4);
            System.out.println(kad5);

            /* Shutting down kad1 and restarting it */
            System.out.println("\n\n\nShutting down Kad instance");
            System.out.println(kad2);
            kad1.shutdown(true);

            System.out.println("\n\n\nReloading Kad instance from file");
            KadPeer kadR2 = KadPeer.loadFromFile("JoshuaK");
            System.out.println(kadR2);
        }
        catch (IllegalStateException e)
        {
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        new SaveStateTest();
    }
}
