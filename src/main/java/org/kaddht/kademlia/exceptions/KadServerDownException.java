package org.kaddht.kademlia.exceptions;

/**
 * KadServer 服务异常
 *
 * @author Lontow
 * @created 20140428
 */
public class KadServerDownException extends RoutingException
{

    public KadServerDownException()
    {
        super();
    }

    public KadServerDownException(String message)
    {
        super(message);
    }
}
