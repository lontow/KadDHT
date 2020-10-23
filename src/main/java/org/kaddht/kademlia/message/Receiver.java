package org.kaddht.kademlia.message;

import java.io.IOException;

/**
 *Receiver
 *
 * @author Lontow
 * @created 20201020
 */
public interface Receiver
{

    /**
     * 消息处理函数
     *
     * @param conversationId
     * @param incoming
     *
     * @throws java.io.IOException
     */
    public void receive(Message incoming, int conversationId) throws IOException;

    /**
     * 超时函数
     *
     * @param conversationId
     *
     * @throws IOException
     * */
    public void timeout(int conversationId) throws IOException;
}
