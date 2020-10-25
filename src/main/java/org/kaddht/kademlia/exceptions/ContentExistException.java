package org.kaddht.kademlia.exceptions;

/**
 * 文件已存在异常
 *
 * @author Lontow
 * @created 20201015
 */
public class ContentExistException extends Exception
{

    public ContentExistException()
    {
        super();
    }

    public ContentExistException(String message)
    {
        super(message);
    }
}
