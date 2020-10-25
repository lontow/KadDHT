package tests;

import java.io.IOException;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.node.KademliaId;

/**
 * Testing connecting 2 nodes to each other
 *
 * @author Lontow
 * @created 20201015
 */
public class
NodeConnectionTest
{

    public static void main(String[] args)
    {
        try
        {
            /* Setting up 2 Kad networks */
            KadPeer kad1 = new KadPeer("JoshuaK", new KademliaId("ASF45678947584567467"), 7574);
            System.out.println("Created Node Kad 1: " + kad1.getNode().getNodeId());

            KadPeer kad2 = new KadPeer("Crystal", new KademliaId("ASERTKJDHGVHERJHGFLK"), 7572);
            //NodeId diff12 = kad1.getNode().getNodeId().xor(kad2.getNode().getNodeId());
            System.out.println("Created Node Kad 2: " + kad2.getNode().getNodeId());
//            System.out.println(kad1.getNode().getNodeId() + " ^ " + kad2.getNode().getNodeId() + " = " + diff12);
//            System.out.println("Kad 1 - Kad 2 distance: " + diff12.getFirstSetBitIndex());

            /* Connecting 2 to 1 */
            System.out.println("Connecting Kad 1 and Kad 2");
            kad1.connect(kad2.getNode());

//            System.out.println("Kad 1: ");
//            System.out.println(kad1.getNode().getRoutingTable());
//            System.out.println("Kad 2: ");
//            System.out.println(kad2.getNode().getRoutingTable());

            /* Creating a new node 3 and connecting it to 1, hoping it'll get onto 2 also */
            KadPeer kad3 = new KadPeer("Jessica", new KademliaId("ASERTKJDOLKMNBVFR45G"), 7783);
            System.out.println("\n\n\n\n\n\nCreated Node Kad 3: " + kad3.getNode().getNodeId());

            System.out.println("Connecting Kad 3 and Kad 2");
            kad3.connect(kad2.getNode());

//            NodeId diff32 = kad3.getNode().getNodeId().xor(kad2.getNode().getNodeId());
//            NodeId diff31 = kad1.getNode().getNodeId().xor(kad3.getNode().getNodeId());
//            System.out.println("Kad 3 - Kad 1 distance: " + diff31.getFirstSetBitIndex());
//            System.out.println("Kad 3 - Kad 2 distance: " + diff32.getFirstSetBitIndex());
            KadPeer kad4 = new KadPeer("Sandy", new KademliaId("ASERTK85OLKMN85FR4SS"), 7789);
            System.out.println("\n\n\n\n\n\nCreated Node Kad 4: " + kad4.getNode().getNodeId());

            System.out.println("Connecting Kad 4 and Kad 2");
            kad4.connect(kad2.getNode());

            System.out.println("\n\nKad 1: " + kad1.getNode().getNodeId() + " Routing Table: ");
            System.out.println(kad1.getRoutingTable());
            System.out.println("\n\nKad 2: " + kad2.getNode().getNodeId() + " Routing Table: ");
            System.out.println(kad2.getRoutingTable());
            System.out.println("\n\nKad 3: " + kad3.getNode().getNodeId() + " Routing Table: ");
            System.out.println(kad3.getRoutingTable());
            System.out.println("\n\nKad 4: " + kad4.getNode().getNodeId() + " Routing Table: ");
            System.out.println(kad4.getRoutingTable());
            
            System.out.println("-------------------------------------------------");
            System.out.println(kad4.getNode().getSocketAddress().getHostName()+":"+kad4.getNode().getSocketAddress().getPort());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
