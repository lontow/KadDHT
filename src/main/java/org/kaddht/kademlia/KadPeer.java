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
 * 网络上的节点
 *
 * @author Lontow
 * @since 20201020
 *
 */
public class KadPeer implements KademliaNode
{


    private final String ownerId;


    private final transient Node localNode;
    private final transient KadServer server;
    private final transient KademliaDHT dht;
    private transient KademliaRoutingTable routingTable;
    private final int udpPort;
    private transient KadConfiguration config;

    private transient Timer refreshOperationTimer;
    private transient TimerTask refreshOperationTTask;

    private final transient MessageFactory messageFactory;


    private final transient Statistician statistician;

    
    {
        statistician = new Statistician();
    }

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
                DefaultConfiguration.getInstance()
        );
    }

    /**
     *
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static KadPeer loadFromFile(String ownerId) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        return KadPeer.loadFromFile(ownerId, DefaultConfiguration.getInstance());
    }
    /*
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static KadPeer loadFromFile(String ownerId, KadConfiguration iconfig) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        DataInputStream din;

        System.out.println("Recovering KadPeer");
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "kad.kns"));
        KadPeer ikad = new JsonSerializer<KadPeer>().read(din);

        System.out.println("Recovering RouteTable");
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "routingtable.kns"));
        KademliaRoutingTable irtbl = new JsonRoutingTableSerializer(iconfig).read(din);

        System.out.println("Recovering Node");
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "node.kns"));
        Node inode = new JsonSerializer<Node>().read(din);

        System.out.println("Recovering DHT");
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
    public synchronized final void connect(Node n) throws IOException, RoutingException
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
        //System.out.println("put:gp"+gp);
        byte[] json=new Gson().toJson(gp).getBytes();
        ;
        System.out.println("put:"+Base64.encode(json)+"\n you can get file using this key!");
        //System.out.println(new String(json));
        return this.put(new KadStorageEntry(content));
    }
    @Override
    public int put(KadStorageEntry entry) throws IOException
    {
        StoreOperation sop = new StoreOperation(this.server, this, entry, this.dht, this.config);
        sop.execute();



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
            //System.out.println("get:gp"+new String(decoded));
            return this.get(gp);
    }
    @Override
    public KadStorageEntry get(GetParameter param) throws NoSuchElementException, IOException, ContentNotFoundException
    {
        if (this.dht.contains(param))
        {
           //本地有保存
            System.out.println("已保存");
            return this.dht.get(param);
        }

        long startTime = System.nanoTime();
        System.out.println("开始查找");
        ContentLookupOperation clo = new ContentLookupOperation(server, this, param, this.config);
        clo.execute();
        System.out.println("查找完成");
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
        this.server.shutdown();
        this.stopRefreshOperation();
        DefaultConfiguration.savestatus=saveState;

        /* 保存 */
        if (saveState)
        {

            this.saveKadState();
        }
    }

    @Override
    public void saveKadState() throws IOException
    {
        DataOutputStream dout;


        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "kad.kns"));
        System.out.println("path:"+getStateStorageFolderName(this.ownerId, this.config) + File.separator + "kad.kns");
        new JsonSerializer<KadPeer>().write(this, dout);

        //保存节点信息
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "node.kns"));
        new JsonSerializer<Node>().write(this.localNode, dout);

        //保存路由表
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "routingtable.kns"));
        new JsonRoutingTableSerializer(this.config).write(this.getRoutingTable(), dout);
        //保存DHT
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "dht.kns"));
        new JsonDHTSerializer().write(this.dht, dout);

    }


    private static String getStateStorageFolderName(String ownerId, KadConfiguration iconfig)
    {
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
