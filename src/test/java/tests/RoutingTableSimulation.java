package tests;

import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.routing.KademliaRoutingTable;

/**
 * Testing how the routing table works and checking if everything works properly
 *
 * @author Lontow
 * @since 20201020
 */
public class RoutingTableSimulation
{

    public RoutingTableSimulation()
    {
        try
        {
            /* Setting up 2 Kad networks */
            KadPeer kad1 = new KadPeer("JoshuaK", new KademliaId("ASF45678947584567463"), 12049);
            KadPeer kad2 = new KadPeer("Crystal", new KademliaId("ASF45678947584567464"), 4585);
            KadPeer kad3 = new KadPeer("Shameer", new KademliaId("ASF45678947584567465"), 8104);
            KadPeer kad4 = new KadPeer("Lokesh", new KademliaId("ASF45678947584567466"), 8335);
            KadPeer kad5 = new KadPeer("Chandu", new KademliaId("ASF45678947584567467"), 13345);

            KademliaRoutingTable rt = kad1.getRoutingTable();
            
            rt.insert(kad2.getNode());
            rt.insert(kad3.getNode());
            rt.insert(kad4.getNode());
            System.out.println(rt);
            
            rt.insert(kad5.getNode());            
            System.out.println(rt);
            
            rt.insert(kad3.getNode());            
            System.out.println(rt);
            
            
            /* Lets shut down a node and then try putting a content on the network. We'll then see how the un-responsive contacts work */
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
        new RoutingTableSimulation();
    }
}
