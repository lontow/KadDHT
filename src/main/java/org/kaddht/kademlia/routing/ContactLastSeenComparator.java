package org.kaddht.kademlia.routing;

import java.util.Comparator;

/**
 * 比较　lastSeenTime
 *
 * @author Lontow
 * @since 20201020
 */
public class ContactLastSeenComparator implements Comparator<Contact>
{

    /**
     *
     *
     * @param c1 Contact 1
     * @param c2 Contact 2
     */
    @Override
    public int compare(Contact c1, Contact c2)
    {
        if (c1.getNode().equals(c2.getNode()))
        {
            return 0;
        }
        else
        {

            return c1.lastSeen() > c2.lastSeen() ? 1 : -1;
        }
    }
}
