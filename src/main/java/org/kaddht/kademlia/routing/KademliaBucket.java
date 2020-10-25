package org.kaddht.kademlia.routing;

import java.util.List;
import org.kaddht.kademlia.node.Node;

/**
 *
 *
 * @author Lontow
 * @created 20201015
 */
public interface KademliaBucket
{

    /**
     *
     *
     * @param c
     */
    public void insert(Contact c);


    public void insert(Node n);


    public boolean containsContact(Contact c);

    public boolean containsNode(Node n);


    public boolean removeContact(Contact c);

    public boolean removeNode(Node n);


    public int numContacts();

    public int getDepth();

    public List<Contact> getContacts();
}
