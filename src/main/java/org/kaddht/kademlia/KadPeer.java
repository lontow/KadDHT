package org.kaddht.kademlia;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import org.kaddht.kademlia.dht.GetParameter;
import org.kaddht.kademlia.dht.DHT;
import org.kaddht.kademlia.dht.KadContent;
import org.kaddht.kademlia.dht.KademliaDHT;
import org.kaddht.kademlia.dht.KadStorageEntry;
import org.kaddht.kademlia.exceptions.ContentNotFoundException;
import org.kaddht.kademlia.exceptions.RoutingException;
import org.kaddht.kademlia.message.MessageFactory;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.operation.ConnectOperation;
import org.kaddht.kademlia.operation.ContentLookupOperation;
import org.kaddht.kademlia.operation.Operation;
import org.kaddht.kademlia.operation.KadRefreshOperation;
import org.kaddht.kademlia.operation.StoreOperation;
import org.kaddht.kademlia.routing.KadRoutingTable;
import org.kaddht.kademlia.routing.KademliaRoutingTable;
import org.kaddht.kademlia.util.Base64;
import org.kaddht.kademlia.util.serializer.JsonDHTSerializer;
import org.kaddht.kademlia.util.serializer.JsonRoutingTableSerializer;
import org.kaddht.kademlia.util.serializer.JsonSerializer;

/**
 * The main Kademlia Node on the network, this node manages everything for this local system.
 *
 * @author Lontow
 * @since 20201020
 *
 * @todo When we receive a store message - if we have a newer version of the content, re-send this newer version to that node so as to update their version
 * @todo Handle IPv6 Addresses
 *
 */
public class KadPeer implements KademliaNode
{

    /* Kademlia Attributes */
    private final String ownerId;

    /* Objects to be used */
    private final transient Node localNode;
    private final transient KadServer server;
    private final transient KademliaDHT dht;
    private transient KademliaRoutingTable routingTable;
    private final int udpPort;
    private transient KadConfiguration config;

    /* Timer used to execute refresh operations */
    private transient Timer refreshOperationTimer;
    private transient TimerTask refreshOperationTTask;

    /* Factories */
    private final transient MessageFactory messageFactory;

    /* Statistics */
    private final transient Statistician statistician;

    
    {
        statistician = new Statistician();
    }

    /**
     * Creates a Kademlia DistributedMap using the specified name as filename base.
     * If the id cannot be read from disk the specified defaultId is used.
     * The instance is bootstraped to an existing network by specifying the
     * address of a bootstrap node in the network.
     *
     * @param ownerId      The Name of this node used for storage
     * @param localNode    The Local Node for this Kad instance
     * @param udpPort      The UDP port to use for routing messages
     * @param dht          The DHT for this instance
     * @param config
     * @param routingTable
     *
     * @throws IOException If an error occurred while reading id or local map
     *                     from disk <i>or</i> a network error occurred while
     *                     attempting to bootstrap to the network
     * */
    public KadPeer(String ownerId, Node localNode, int udpPort, KademliaDHT dht, KademliaRoutingTable routingTable, KadConfiguration config) throws IOException
    {
        this.ownerId = ownerId;
        this.udpPort = udpPort;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
        this.routingTable = routingTable;
        this.messageFactory = new MessageFactory(this, this.dht, this.config);
        this.server = new KadServer(udpPort, this.messageFactory, this.localNode, this.config, this.statistician);
        this.startRefreshOperation();
    }

    @Override
    public final void startRefreshOperation()
    {
        this.refreshOperationTimer = new Timer(true);
        refreshOperationTTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    /* Runs a DHT RefreshOperation  */
                    KadPeer.this.refresh();
                }
                catch (IOException e)
                {
                    System.err.println("KademliaNode: Refresh Operation Failed; Message: " + e.getMessage());
                }
            }
        };
        refreshOperationTimer.schedule(refreshOperationTTask, this.config.restoreInterval(), this.config.restoreInterval());
    }

    @Override
    public final void stopRefreshOperation()
    {
        /* Close off the timer tasks */
        this.refreshOperationTTask.cancel();
        this.refreshOperationTimer.cancel();
        this.refreshOperationTimer.purge();
    }

    public KadPeer(String ownerId, Node node, int udpPort, KademliaRoutingTable routingTable, KadConfiguration config) throws IOException
    {
        this(
                ownerId,
                node,
                udpPort,
                new DHT(ownerId, config),
                routingTable,
                config
        );
    }

    public KadPeer(String ownerId, Node node, int udpPort, KadConfiguration config) throws IOException
    {
        this(
                ownerId,
                node,
                udpPort,
                new KadRoutingTable(node, config),
                config
        );
    }

    public KadPeer(String ownerId, KademliaId defaultId, int udpPort) throws IOException
    {
        this(
                ownerId,
                new Node(defaultId, InetAddress.getLocalHost(), udpPort),
                udpPort,
                new DefaultConfiguration()
        );
    }

    /**
     * Load Stored state using default configuration
     *
     * @param ownerId The ID of the owner for the stored state
     *
     * @return A Kademlia instance loaded from a stored state in a file
     *
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static KadPeer loadFromFile(String ownerId) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        return KadPeer.loadFromFile(ownerId, new DefaultConfiguration());
    }

    /**
     * Load Stored state
     *
     * @param ownerId The ID of the owner for the stored state
     * @param iconfig Configuration information to work with
     *
     * @return A Kademlia instance loaded from a stored state in a file
     *
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static KadPeer loadFromFile(String ownerId, KadConfiguration iconfig) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        DataInputStream din;

        /**
         * @section Read Basic Kad data
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "kad.kns"));
        KadPeer ikad = new JsonSerializer<KadPeer>().read(din);

        /**
         * @section Read the routing table
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "routingtable.kns"));
        KademliaRoutingTable irtbl = new JsonRoutingTableSerializer(iconfig).read(din);

        /**
         * @section Read the node state
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "node.kns"));
        Node inode = new JsonSerializer<Node>().read(din);

        /**
         * @section Read the DHT
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "dht.kns"));
        KademliaDHT idht = new JsonDHTSerializer().read(din);
        idht.setConfiguration(iconfig);

        return new KadPeer(ownerId, inode, ikad.getPort(), idht, irtbl, iconfig);
    }

    @Override
    public Node getNode()
    {
        return this.localNode;
    }

    @Override
    public KadServer getServer()
    {
        return this.server;
    }

    @Override
    public KademliaDHT getDHT()
    {
        return this.dht;
    }

    @Override
    public KadConfiguration getCurrentConfiguration()
    {
        return this.config;
    }

    @Override
    public synchronized final void bootstrap(Node n) throws IOException, RoutingException
    {
        long startTime = System.nanoTime();
        Operation op = new ConnectOperation(this.server, this, n, this.config);
        op.execute();
        long endTime = System.nanoTime();
        this.statistician.setBootstrapTime(endTime - startTime);
    }

    @Override
    public int put(KadContent content) throws IOException
    {
        GetParameter gp= new GetParameter(content.getKey(),content.getType(),content.getOwnerId());
        System.out.println("put:gp"+gp);
        byte[] json=new Gson().toJson(gp).getBytes();
        ;
        System.out.println("put:"+Base64.encode(json)+"\n you can get file using this key!");
        System.out.println(new String(json));
        return this.put(new KadStorageEntry(content));
    }
    @Override
    public int put(KadStorageEntry entry) throws IOException
    {
        StoreOperation sop = new StoreOperation(this.server, this, entry, this.dht, this.config);
        sop.execute();


        /* Return how many nodes the content was stored on */
        return sop.numNodesStoredAt();
    }

    @Override
    public void putLocally(KadContent content) throws IOException
    {
        this.dht.store(new KadStorageEntry(content));
    }

    public KadStorageEntry get(String cipher) throws IOException, ContentNotFoundException {
            byte[] decoded= Base64.decode(cipher);
            GetParameter gp=new Gson().fromJson(new String(decoded),GetParameter.class);
            System.out.println("get:gp"+new String(decoded));
            return this.get(gp);
    }
    @Override
    public KadStorageEntry get(GetParameter param) throws NoSuchElementException, IOException, ContentNotFoundException
    {
        if (this.dht.contains(param))
        {
            /* If the content exist in our own DHT, then return it. */
            return this.dht.get(param);
        }

        /* Seems like it doesn't exist in our DHT, get it from other Nodes */
        long startTime = System.nanoTime();
        ContentLookupOperation clo = new ContentLookupOperation(server, this, param, this.config);
        clo.execute();
        long endTime = System.nanoTime();
        this.statistician.addContentLookup(endTime - startTime, clo.routeLength(), clo.isContentFound());
        return clo.getContentFound();
    }

    @Override
    public void refresh() throws IOException
    {
        new KadRefreshOperation(this.server, this, this.dht, this.config).execute();
    }

    @Override
    public String getOwnerId()
    {
        return this.ownerId;
    }

    @Override
    public int getPort()
    {
        return this.udpPort;
    }

    @Override
    public void shutdown(final boolean saveState) throws IOException
    {
        /* Shut down the server */
        this.server.shutdown();

        this.stopRefreshOperation();

        /* Save this Kademlia instance's state if required */
        if (saveState)
        {
            /* Save the system state */
            this.saveKadState();
        }
    }

    @Override
    public void saveKadState() throws IOException
    {
        DataOutputStream dout;

        /**
         * @section Store Basic Kad data
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "kad.kns"));
        System.out.println("path:"+getStateStorageFolderName(this.ownerId, this.config) + File.separator + "kad.kns");
        new JsonSerializer<KadPeer>().write(this, dout);

        /**
         * @section Save the node state
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "node.kns"));
        new JsonSerializer<Node>().write(this.localNode, dout);

        /**
         * @section Save the routing table
         * We need to save the routing table separate from the node since the routing table will contain the node and the node will contain the routing table
         * This will cause a serialization recursion, and in turn a Stack Overflow
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "routingtable.kns"));
        new JsonRoutingTableSerializer(this.config).write(this.getRoutingTable(), dout);

        /**
         * @section Save the DHT
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "dht.kns"));
        new JsonDHTSerializer().write(this.dht, dout);

    }

    /**
     * Get the name of the folder for which a content should be stored
     *
     * @return String The name of the folder to store node states
     */
    private static String getStateStorageFolderName(String ownerId, KadConfiguration iconfig)
    {
        /* Setup the nodes storage folder if it doesn't exist */
        String path = iconfig.getNodeDataFolder(ownerId) + File.separator + "nodeState";
        File nodeStateFolder = new File(path);
        if (!nodeStateFolder.isDirectory())
        {
            nodeStateFolder.mkdir();
        }
        return nodeStateFolder.toString();
    }

    @Override
    public KademliaRoutingTable getRoutingTable()
    {
        return this.routingTable;
    }

    @Override
    public Statistician getStatistician()
    {
        return this.statistician;
    }

    /**
     * Creates a string containing all data about this Kademlia instance
     *
     * @return The string representation of this Kad instance
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("\n\nPrinting Kad State for instance with owner: ");
        sb.append(this.ownerId);
        sb.append("\n\n");

        sb.append("\n");
        sb.append("Local Node");
        sb.append(this.localNode);
        sb.append("\n");

        sb.append("\n");
        sb.append("Routing Table: ");
        sb.append(this.getRoutingTable());
        sb.append("\n");

        sb.append("\n");
        sb.append("DHT: ");
        sb.append(this.dht);
        sb.append("\n");

        sb.append("\n\n\n");

        return sb.toString();
    }
}
