package org.kaddht.kademlia.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.kaddht.kademlia.message.Streamable;

/**
 * Node 相关信息
 *
 * @author Lontow
 * @since 20201020
 * @version 0.1
 */
public class Node implements Streamable, Serializable
{

    private KademliaId nodeId;
    private InetAddress inetAddress;
    private int port;
    private final String strRep;

    public Node(KademliaId nid, InetAddress ip, int port)
    {
        this.nodeId = nid;
        this.inetAddress = ip;
        this.port = port;
        this.strRep = this.nodeId.toString();
    }

    /**
     * 从输入流中创建　Node
     *
     * @param in
     *
     * @throws IOException
     */
    public Node(DataInputStream in) throws IOException
    {
        this.fromStream(in);
        this.strRep = this.nodeId.toString();
    }

    /**
     * 设置　addr
     *
     * @param addr The
     */
    public void setInetAddress(InetAddress addr)
    {
        this.inetAddress = addr;
    }

    public KademliaId getNodeId()
    {
        return this.nodeId;
    }

    /**
     * 创建　SocketAddress
     *
     * @return
     */
    public InetSocketAddress getSocketAddress()
    {
        return new InetSocketAddress(this.inetAddress, this.port);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.nodeId.toStream(out);


        byte[] a = inetAddress.getAddress();
        if (a.length != 4)
        {
            throw new RuntimeException("Expected InetAddress of 4 bytes, got " + a.length);
        }
        out.write(a);

        out.writeInt(port);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {

        this.nodeId = new KademliaId(in);


        byte[] ip = new byte[4];
        in.readFully(ip);
        this.inetAddress = InetAddress.getByAddress(ip);

        this.port = in.readInt();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Node)
        {
            Node n = (Node) o;
            if (n == this)
            {
                return true;
            }
            return this.getNodeId().equals(n.getNodeId());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getNodeId().hashCode();
    }

    @Override
    public String toString()
    {
        return this.getNodeId().toString();
    }
}
