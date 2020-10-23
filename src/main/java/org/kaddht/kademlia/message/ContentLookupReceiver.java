package org.kaddht.kademlia.message;

import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.dht.KademliaDHT;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * 对　ContentLookupOperation 的回应，返回其寻找的内容
 * 如果查询的内容不存在，返回一个　NodeReplyMessage 消息
 *
 * @author Lontow
 * @since 20201020
 */
public class ContentLookupReceiver implements Receiver
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;
    private final KadConfiguration config;

    public ContentLookupReceiver(KadServer server, KademliaNode localNode, KademliaDHT dht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
    }

    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        ContentLookupMessage msg = (ContentLookupMessage) incoming;
        this.localNode.getRoutingTable().insert(msg.getOrigin());

        /* 检查是否含有该数据 */
        if (this.dht.contains(msg.getParameters()))
        {
            try
            {
                /* 返回一个带有该数据的消息 */
                ContentMessage cMsg = new ContentMessage(localNode.getNode(), this.dht.get(msg.getParameters()));
                server.reply(msg.getOrigin(), cMsg, comm);
            }
            catch (NoSuchElementException ex)
            {
                /* */
            }
        }
        else
        {
            /**
             * 返回距内容标识符最近的　K 个Node.通过 NodeLookupReceiver 实现
             */
            NodeLookupMessage lkpMsg = new NodeLookupMessage(msg.getOrigin(), msg.getParameters().getKey());
            new NodeLookupReceiver(server, localNode, this.config).receive(lkpMsg, comm);
        }
    }

    @Override
    public void timeout(int comm)
    {

    }
}
