package org.kaddht.kademlia.exceptions;

/**
 * 未知的消息类型
 *
 * @author Lontow
 * @created 20201015
 */
public class UnknownMessageException extends RuntimeException
{

    public UnknownMessageException()
    {
        super();
    }

    public UnknownMessageException(String message)
    {
        super(message);
    }
}
