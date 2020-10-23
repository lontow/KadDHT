package org.kaddht.kademlia.message;

import org.kaddht.kademlia.KadServer;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 消息工厂，用于处理消息的发送和接受
 *
 * @author Lontow
 * @since 20201020
 */
public interface KademliaMessageFactory
{

    /**
     * 创建消息
     *
     * @param code 消息码
     * @param in   输入流
     *
     * @return 消息对象
     *
     * @throws java.io.IOException
     */
    public Message createMessage(byte code, DataInputStream in) throws IOException;

    /**
     * 返回一个处理消息的 Receiver
     *
     * @param code   消息码
     * @param server
     *
     * @return A receiver
     */
    public Receiver createReceiver(byte code, KadServer server);
}
