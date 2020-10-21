package org.kaddht.kademlia.operation;

import java.io.IOException;
import org.kaddht.kademlia.exceptions.RoutingException;

/**
 * An operation in the Kademlia routing protocol
 *
 * @author Lontow
 * @created 20201020
 */
public interface Operation
{

    /**
     * Starts an operation and returns when the operation is finished
     *
     * @throws org.kaddht.kademlia.exceptions.RoutingException
     */
    public void execute() throws IOException, RoutingException;
}
