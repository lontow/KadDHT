package org.kaddht.kademlia.exceptions;

import java.io.IOException;

/**
 * An exception to be thrown whenever there is a routing problem
 *
 * @author Lontow
 * @created 20140219
 */
public class RoutingException extends IOException
{

    public RoutingException()
    {
        super();
    }

    public RoutingException(String message)
    {
        super(message);
    }
}
