package org.kaddht.kademlia.node;

import java.math.BigInteger;
import java.util.Comparator;

/**
 * 比较 NodeID
 *
 * @author Lontow
 * @since 20201020
 */
public class KeyComparator implements Comparator<Node>
{

    private final BigInteger key;

    /**
     * @param key 需要比较距离的 NodeID
     */
    public KeyComparator(KademliaId key)
    {
        this.key = key.getInt();
    }

    /**
     *
     *
     * @param n1 Node 1
     * @param n2 Node 2
     */
    @Override
    public int compare(Node n1, Node n2)
    {
        BigInteger b1 = n1.getNodeId().getInt();
        BigInteger b2 = n2.getNodeId().getInt();

        b1 = b1.xor(key);
        b2 = b2.xor(key);

        return b1.abs().compareTo(b2.abs());
    }
}
