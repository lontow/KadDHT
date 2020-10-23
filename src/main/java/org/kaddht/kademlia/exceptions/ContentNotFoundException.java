package org.kaddht.kademlia.exceptions;

/**
 * 文件不存在异常
 *
 * @author Lontow
 * @created 20140322
 */
public class ContentNotFoundException extends Exception
{

    public ContentNotFoundException()
    {
        super();
    }

    public ContentNotFoundException(String message)
    {
        //super(message);
        System.out.println(message);
    }
}
