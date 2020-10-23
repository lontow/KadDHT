/**
 * Implementation of the Kademlia Ping operation,
 * This is on hold at the moment since I'm not sure if we'll use ping given the improvements mentioned in the paper.
 *
 * @author Lontow
 * @since 20201020
 */
package org.kaddht.kademlia.operation;

import java.io.IOException;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.exceptions.RoutingException;
import org.kaddht.kademlia.node.Node;

public class PingOperation implements Operation
{

    private final KadServer server;
    private final Node localNode;
    private final Node toPing;

    /**
     * @param server Kademlia服务器用于发送和接收消息
     * @param local  本地节点
     * @param toPing 将ping消息发送到的节点
     */
    public PingOperation(KadServer server, Node local, Node toPing)
    {
        this.server = server;
        this.localNode = local;
        this.toPing = toPing;
    }

    @Override
    public void execute() throws IOException, RoutingException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
