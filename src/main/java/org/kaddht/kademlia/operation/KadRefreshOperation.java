package org.kaddht.kademlia.operation;

import java.io.IOException;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.dht.KademliaDHT;

/**
 * 用来刷新整个Kademlia系统（包括存储桶和内容）的操作A
 *
 * @author 刘朕龙
 * @since 20201015
 */
public class KadRefreshOperation implements Operation
{

    private final KadServer server;
    private final KademliaNode localNode;
    private final KademliaDHT dht;
    private final KadConfiguration config;

    public KadRefreshOperation(KadServer server, KademliaNode localNode, KademliaDHT dht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
    }

    @Override
    public void execute() throws IOException
    {
        // 运行存储桶刷新操作以刷新存储桶
        new BucketRefreshOperation(this.server, this.localNode, this.config).execute();

        // 刷新内容
        new ContentRefreshOperation(this.server, this.localNode, this.dht, this.config).execute();
    }
}
