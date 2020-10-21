package org.kaddht.kademlia.operation;

import java.io.IOException;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.KadServer;
import org.kaddht.kademlia.KademliaNode;
import org.kaddht.kademlia.dht.KademliaDHT;

/**
 * An operation that handles refreshing the entire Kademlia Systems including buckets and content
 *
 * @author Lontow
 * @since 20201020
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
        /* Run our BucketRefreshOperation to refresh buckets */
        new BucketRefreshOperation(this.server, this.localNode, this.config).execute();

        /* After buckets have been refreshed, we refresh content */
        new ContentRefreshOperation(this.server, this.localNode, this.dht, this.config).execute();
    }
}
