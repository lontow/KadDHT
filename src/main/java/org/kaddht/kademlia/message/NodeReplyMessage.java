package org.kaddht.kademlia.message;

import org.kaddht.kademlia.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * NodeLookup 请求回复消息类型
 *
 * @author Lontow
 * @created 20201020
 */
public class NodeReplyMessage implements Message
{

    private Node origin;
    public static final byte CODE = 0x06;
    private List<Node> nodes;

    public NodeReplyMessage(Node origin, List<Node> nodes)
    {
        this.origin = origin;
        this.nodes = nodes;
    }

    public NodeReplyMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }


    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);

        int len = in.readInt();
        this.nodes = new ArrayList<Node>(len);

        /* 读取所有节点 */
        for (int i = 0; i < len; i++)
        {
            this.nodes.add(new Node(in));
        }
    }

    public void toStream(DataOutputStream out) throws IOException
    {
        origin.toStream(out);

        int len = this.nodes.size();
        if (len > 255)
        {
            throw new IndexOutOfBoundsException("Too many nodes in list to send in NodeReplyMessage. Size: " + len);
        }


        out.writeInt(len);
        for (Node n : this.nodes)
        {
            n.toStream(out);
        }
    }

    public Node getOrigin()
    {
        return this.origin;
    }


    public byte code()
    {
        return CODE;
    }

    public List<Node> getNodes()
    {
        return this.nodes;
    }

    @Override
    public String toString()
    {
        return "NodeReplyMessage[origin NodeId=" + origin.getNodeId() + "]";
    }
}
