package org.kaddht.kademlia.exceptions;

import java.io.IOException;

/**
 * 路由异常
 *
 * @author Lontow
 * @created 20201015
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
