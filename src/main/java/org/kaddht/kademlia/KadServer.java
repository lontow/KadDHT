package org.kaddht.kademlia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.kaddht.kademlia.exceptions.KadServerDownException;
import org.kaddht.kademlia.message.KademliaMessageFactory;
import org.kaddht.kademlia.message.Message;
import org.kaddht.kademlia.message.MessageFactory;
import org.kaddht.kademlia.node.Node;
import org.kaddht.kademlia.message.Receiver;

/**
 * 收发　message
 *
 * @author Lontow
 * @created 20140215
 */
public class KadServer
{

    /* 数据包最大字节数 */
    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;      // 64KB


    private final transient KadConfiguration config;


    private final DatagramSocket socket;//UDP
    private transient boolean isRunning;
    private final Map<Integer, Receiver> receivers;
    private final Timer timer;      // 调度器
    private final Map<Integer, TimerTask> tasks;    //

    private final Node localNode;


    private final KademliaMessageFactory messageFactory;

    private final Statistician statistician;

    
    {
        isRunning = true;
        this.tasks = new HashMap<>();
        this.receivers = new HashMap<>();
        this.timer = new Timer(true);
    }

    /**
     * 初始化
     *
     * @param udpPort      端口
     * @param mFactory     消息工厂
     * @param localNode    本地Node
     * @param config
     * @param statistician 统计信息
     *
     * @throws java.net.SocketException
     */
    public KadServer(int udpPort, KademliaMessageFactory mFactory, Node localNode, KadConfiguration config, Statistician statistician) throws SocketException
    {
        this.config = config;
        this.socket = new DatagramSocket(udpPort);
        this.localNode = localNode;
        this.messageFactory = mFactory;
        this.statistician = statistician;


        this.startListener();
    }


    private void startListener()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                listen();
            }
        }.start();
    }

    /**
     * 发送信息
     *
     * @param msg  消息
     * @param to   目的地
     * @param recv 接受器
     *
     * @return commuicationID
     *
     * @throws IOException
     * @throws org.kaddht.kademlia.exceptions.KadServerDownException
     */
    public synchronized int sendMessage(Node to, Message msg, Receiver recv) throws IOException, KadServerDownException
    {
        if (!isRunning)
        {
            throw new KadServerDownException(this.localNode + " - Kad Server is not running.");
        }

        /* 随机 communication ID */
        int comm = new Random().nextInt();

        /* 接受 */
        if (recv != null)
        {
            try
            {
                /* 设置回复 */
                receivers.put(comm, recv);
                TimerTask task = new TimeoutTask(comm, recv);
                timer.schedule(task, this.config.responseTimeout());
                tasks.put(comm, task);
            }
            catch (IllegalStateException ex)
            {

            }
        }


        sendMessage(to, msg, comm);

        return comm;
    }

    /**
     * 回复
     *
     * @param to   目的节点
     * @param msg  消息
     * @param comm commucationID
     *
     * @throws java.io.IOException
     */
    public synchronized void reply(Node to, Message msg, int comm) throws IOException
    {
        if (!isRunning)
        {
            throw new IllegalStateException("Kad Server is not running.");
        }
        sendMessage(to, msg, comm);
    }


    private void sendMessage(Node to, Message msg, int comm) throws IOException
    {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); DataOutputStream dout = new DataOutputStream(bout);)
        {

            dout.writeInt(comm);
            dout.writeByte(msg.code());
            msg.toStream(dout);
            dout.close();

            byte[] data = bout.toByteArray();

            if (data.length > DATAGRAM_BUFFER_SIZE)
            {
                throw new IOException("Message is too big");
            }

            /* 创建包并发送*/
            DatagramPacket pkt = new DatagramPacket(data, 0, data.length);
            pkt.setSocketAddress(to.getSocketAddress());
            socket.send(pkt);

            /* 统计信息 */
            this.statistician.sentData(data.length);
        }
    }

    /**
     * 监听
     */
    private void listen()
    {
        try
        {
            while (isRunning)
            {
                try
                {

                    byte[] buffer = new byte[DATAGRAM_BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    /* 统计信息*/
                    this.statistician.receivedData(packet.getLength());

                    if (this.config.isTesting())
                    {
                        /**
                         * 模拟延迟
                         */
                        int pause = packet.getLength() / 100;
                        try
                        {
                            Thread.sleep(pause);
                        }
                        catch (InterruptedException ex)
                        {

                        }
                    }

                    /* 收到包 */
                    try (ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                            DataInputStream din = new DataInputStream(bin);)
                    {

                        /* 获取会话ID */
                        int comm = din.readInt();
                        byte messCode = din.readByte();

                        Message msg = messageFactory.createMessage(messCode, din);
                        din.close();

                        Receiver receiver;
                        if (this.receivers.containsKey(comm))
                        {
                            synchronized (this)
                            {
                                receiver = this.receivers.remove(comm);
                                TimerTask task = (TimerTask) tasks.remove(comm);
                                if (task != null)
                                {
                                    task.cancel();
                                }
                            }
                        }
                        else
                        {
                            receiver = messageFactory.createReceiver(messCode, this);
                        }

                        if (receiver != null)
                        {
                            receiver.receive(msg, comm);
                        }
                    }
                }
                catch (IOException e)
                {
                    //this.isRunning = false;
                    System.err.println("Server ran into a problem in listener method. Message: " + e.getMessage());
                }
            }
        }
        finally
        {
            if (!socket.isClosed())
            {
                socket.close();
            }
            this.isRunning = false;
        }
    }

    /**
     * 删除会话
     *
     * @param comm The id
     */
    private synchronized void unregister(int comm)
    {
        receivers.remove(comm);
        this.tasks.remove(comm);
    }


    public synchronized void shutdown()
    {
        this.isRunning = false;
        this.socket.close();
        timer.cancel();
    }

    /**
     * 超时任务
     * */
    class TimeoutTask extends TimerTask
    {

        private final int comm;
        private final Receiver recv;

        public TimeoutTask(int comm, Receiver recv)
        {
            this.comm = comm;
            this.recv = recv;
        }

        @Override
        public void run()
        {
            if (!KadServer.this.isRunning)
            {
                return;
            }

            try
            {
                unregister(comm);
                recv.timeout(comm);
            }
            catch (IOException e)
            {
                System.err.println("Cannot unregister a receiver. Message: " + e.getMessage());
            }
        }
    }

    public void printReceivers()
    {
        for (Integer r : this.receivers.keySet())
        {
            System.out.println("Receiver for comm: " + r + "; Receiver: " + this.receivers.get(r));
        }
    }

    public boolean isRunning()
    {
        return this.isRunning;
    }

}
