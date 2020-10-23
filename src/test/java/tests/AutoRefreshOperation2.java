package tests;

import java.util.Timer;
import java.util.TimerTask;
import org.kaddht.kademlia.DefaultConfiguration;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.dht.DHTContentImpl;
import org.kaddht.kademlia.node.KademliaId;

/**
 * Testing the Kademlia Auto Content and Node table refresh operations
 *
 * @author Lontow
 * @since 20201020
 */
public class AutoRefreshOperation2 implements Simulation
{

    @Override
    public void runSimulation()
    {
        try
        {
            /* Setting up 2 Kad networks */
            final KadPeer kad1 = new KadPeer("JoshuaK", new KademliaId("ASF456789djem4567463"), 12049);
            final KadPeer kad2 = new KadPeer("Crystal", new KademliaId("AS84k678DJRW84567465"), 4585);
            final KadPeer kad3 = new KadPeer("Shameer", new KademliaId("AS84k67894758456746A"), 8104);

            /* Connecting nodes */
            System.out.println("Connecting Nodes");
            kad2.bootstrap(kad1.getNode());
            kad3.bootstrap(kad2.getNode());

            DHTContentImpl c = new DHTContentImpl(new KademliaId("AS84k678947584567465"), kad1.getOwnerId());
            c.setData("Setting the data");
            kad1.putLocally(c);

            System.out.println("\n Content ID: " + c.getKey());
            System.out.println(kad1.getNode() + " Distance from content: " + kad1.getNode().getNodeId().getDistance(c.getKey()));
            System.out.println(kad2.getNode() + " Distance from content: " + kad2.getNode().getNodeId().getDistance(c.getKey()));
            System.out.println(kad3.getNode() + " Distance from content: " + kad3.getNode().getNodeId().getDistance(c.getKey()));
            System.out.println("\nSTORING CONTENT 1 locally on " + kad1.getOwnerId() + "\n\n\n\n");

            System.out.println(kad1);
            System.out.println(kad2);
            System.out.println(kad3);

            /* Print the node states every few minutes */
            KadConfiguration config = new DefaultConfiguration();
            Timer timer = new Timer(true);
            timer.schedule(
                    new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            System.out.println(kad1);
                            System.out.println(kad2);
                            System.out.println(kad3);
                        }
                    },
                    // Delay                        // Interval
                    config.restoreInterval(), config.restoreInterval()
            );
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
