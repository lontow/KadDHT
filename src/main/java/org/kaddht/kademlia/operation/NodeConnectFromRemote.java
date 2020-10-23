package org.kaddht.kademlia.operation;

import java.io.IOException;
import java.net.InetAddress;
import java.util.NoSuchElementException;

import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.dht.GetParameter;
import org.kaddht.kademlia.dht.KademliaStorageEntry;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.dht.DHTContentImpl;

public class NodeConnectFromRemote {
public static void main(String[] args) throws IOException, NoSuchElementException, ContentNotFoundException {
	KadPeer kad = new KadPeer("Lontow2", new KademliaId("ASERTK85OLKMN85FR455"), 7900);
	System.out.println(InetAddress.getLocalHost());
	kad.bootstrap(new Node(new KademliaId("ASF45678947584567467"), InetAddress.getByName("hg1"), 9999));
	System.out.println(kad.getRoutingTable());
	System.out.println(InetAddress.getByName("hg1"));
	
	DHTContentImpl c = new DHTContentImpl(kad.getOwnerId(), "ASF45678947584567467");
    kad.put(c);

    /* Lets retrieve the content */
    System.out.println("Retrieving Content");
    GetParameter gp = new GetParameter(c.getKey(), DHTContentImpl.TYPE, c.getOwnerId());
    
    System.out.println("Get Parameter: " + gp);
    KademliaStorageEntry conte = kad.get(gp);
    System.out.println("Content Found: " + new DHTContentImpl().fromSerializedForm(conte.getContent()));
    System.out.println("Content Metadata: " + conte.getContentMetadata());

    /* Lets update the content and put it again */
    c.setData("Some New Data");
    kad.put(c);

    /* Lets retrieve the content */
    System.out.println("Retrieving Content Again");
    conte = kad.get(gp);
    System.out.println("Content Found: " + new DHTContentImpl().fromSerializedForm(conte.getContent()));
    System.out.println("Content Metadata: " + conte.getContentMetadata());
}
}
