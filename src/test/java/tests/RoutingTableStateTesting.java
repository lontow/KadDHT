package tests;

import java.io.IOException;
import java.util.Scanner;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.dht.DHTContentImpl;
import org.kaddht.kademlia.dht.KadContent;
import org.kaddht.kademlia.node.KademliaId;

/**
 * Testing how the routing table works and it's state after different operations
 *
 * @author Lontow
 * @since 20201020
 */
public class RoutingTableStateTesting
{

    KadPeer[] kads;

    public int numKads = 10;

    public RoutingTableStateTesting()
    {
        try
        {
            /* Setting up Kad networks */
            kads = new KadPeer[numKads];

            kads[0] = new KadPeer("user0", new KademliaId("HRF456789SD584567460"), 1334);
            kads[1] = new KadPeer("user1", new KademliaId("ASF456789475DS567461"), 1209);
            kads[2] = new KadPeer("user2", new KademliaId("AFG45678947584567462"), 4585);
            kads[3] = new KadPeer("user3", new KademliaId("FSF45J38947584567463"), 8104);
            kads[4] = new KadPeer("user4", new KademliaId("ASF45678947584567464"), 8335);
            kads[5] = new KadPeer("user5", new KademliaId("GHF4567894DR84567465"), 13345);
            kads[6] = new KadPeer("user6", new KademliaId("ASF45678947584567466"), 12049);
            kads[7] = new KadPeer("user7", new KademliaId("AE345678947584567467"), 14585);
            kads[8] = new KadPeer("user8", new KademliaId("ASAA5678947584567468"), 18104);
            kads[9] = new KadPeer("user9", new KademliaId("ASF456789475845674U9"), 18335);

            for (int i = 1; i < numKads; i++)
            {
                kads[i].bootstrap(kads[0].getNode());
            }

            /* Lets shut down a node and then try putting a content on the network. We'll then see how the un-responsive contacts work */
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public KadContent putContent(String content, KadPeer owner)
    {
        DHTContentImpl c = null;
        try
        {
            c = new DHTContentImpl(owner.getOwnerId(), "Some Data");
            owner.put(c);
            return c;
        }
        catch (IOException e)
        {
            System.err.println("Error whiles putting content " + content + " from owner: " + owner.getOwnerId());
        }

        return c;
    }

    public void shutdownKad(KadPeer kad)
    {
        try
        {
            kad.shutdown(false);
        }
        catch (IOException ex)
        {
            System.err.println("Error whiles shutting down node with owner: " + kad.getOwnerId());
        }
    }

    public void printRoutingTable(int kadId)
    {
        System.out.println(kads[kadId].getRoutingTable());
    }

    public void printRoutingTables()
    {
        for (int i = 0; i < numKads; i++)
        {
            this.printRoutingTable(i);
        }
    }

    public void printStorage(int kadId)
    {
        System.out.println(kads[kadId].getDHT());
    }

    public void printStorage()
    {
        for (int i = 0; i < numKads; i++)
        {
            this.printStorage(i);
        }
    }

    public static void main(String[] args)
    {

        RoutingTableStateTesting rtss = new RoutingTableStateTesting();

        try
        {
            rtss.printRoutingTables();

            /* Lets shut down a node to test the node removal operation */
            rtss.shutdownKad(rtss.kads[3]);

            rtss.putContent("Content owned by kad0", rtss.kads[0]);
            rtss.printStorage();

            Thread.sleep(1000);

            /* kad3 should be removed from their routing tables by now. */
            rtss.printRoutingTables();
        }
        catch (InterruptedException ex)
        {

        }

        Scanner sc = new Scanner(System.in);
        while (true)
        {
            System.out.println("\n\n ************************* Options **************************** \n");
            System.out.println("1 i - Print routing table of node i");
            int val1 = sc.nextInt();
            int val2 = sc.nextInt();

            switch (val1)
            {
                case 1:
                    rtss.printRoutingTable(val2);
                    break;
            }
        }
    }
}
