/**
 * @author Lontow
 * @created 20140215
 * @desc kadID
 */
package org.kaddht.kademlia.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import org.kaddht.kademlia.message.Streamable;

public class KademliaId implements Streamable, Serializable
{

    public final transient static int ID_LENGTH = 160;
    private byte[] keyBytes;

    /**
     * 根据　string 创建 kadid
     *
     * @param data
     */
    public KademliaId(String data)
    {
        keyBytes = data.getBytes();
        if (keyBytes.length != ID_LENGTH / 8)
        {
            throw new IllegalArgumentException("Specified Data need to be " + (ID_LENGTH / 8) + " characters long.");
        }
    }

    /**
     * 生成随机 key
     */
    public KademliaId()
    {
        keyBytes = new byte[ID_LENGTH / 8];
        new Random().nextBytes(keyBytes);
    }

    /**
     * 从 byte[]生成 key
     *
     * @param bytes
     */
    public KademliaId(byte[] bytes)
    {
        if (bytes.length != ID_LENGTH / 8)
        {
            throw new IllegalArgumentException("Specified Data need to be " + (ID_LENGTH / 8) + " characters long. Data Given: '" + new String(bytes) + "'");
        }
        this.keyBytes = bytes;
    }

    /**
     *
     *
     * @param in
     *
     * @throws IOException
     */
    public KademliaId(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    public byte[] getBytes()
    {
        return this.keyBytes;
    }

    /**
     * @return
     */
    public BigInteger getInt()
    {
        return new BigInteger(1, this.getBytes());
    }

    /**
     * 比较 NodeId
     *
     * @param o
     *
     * @return boolean
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof KademliaId)
        {
            KademliaId nid = (KademliaId) o;
            return this.hashCode() == nid.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Arrays.hashCode(this.keyBytes);
        return hash;
    }

    /**
     * xor 计算距离
     *
     * @param nid
     *
     * @return
     */
    public KademliaId xor(KademliaId nid)
    {
        byte[] result = new byte[ID_LENGTH / 8];
        byte[] nidBytes = nid.getBytes();

        for (int i = 0; i < ID_LENGTH / 8; i++)
        {
            result[i] = (byte) (this.keyBytes[i] ^ nidBytes[i]);
        }

        KademliaId resNid = new KademliaId(result);

        return resNid;
    }

    /**
     * 产生一个距离　distance 的　KadID
     *
     * @param distance 位数
     *
     * @return
     */
    public KademliaId generateNodeIdByDistance(int distance)
    {
        byte[] result = new byte[ID_LENGTH / 8];

        int numByteZeroes = (ID_LENGTH - distance) / 8;
        int numBitZeroes = 8 - (distance % 8);

        /* 补零 */
        for (int i = 0; i < numByteZeroes; i++)
        {
            result[i] = 0;
        }

        /* 位置零 */
        BitSet bits = new BitSet(8);
        bits.set(0, 8);

        for (int i = 0; i < numBitZeroes; i++)
        {
            /* 置零 */
            bits.clear(i);
        }
        bits.flip(0, 8);        // 顺序反转
        result[numByteZeroes] = (byte) bits.toByteArray()[0];

        /* 将剩下的字节置为最大值 */
        for (int i = numByteZeroes + 1; i < result.length; i++)
        {
            result[i] = Byte.MAX_VALUE;
        }

        return this.xor(new KademliaId(result));
    }

    /**
     * 计算前缀零
     *
     * @return 前缀零的个数
     */
    public int getFirstSetBitIndex()
    {
        int prefixLength = 0;

        for (byte b : this.keyBytes)
        {
            if (b == 0)
            {
                prefixLength += 8;
            }
            else
            {

                int count = 0;
                for (int i = 7; i >= 0; i--)
                {
                    boolean a = (b & (1 << i)) == 0;
                    if (a)
                    {
                        count++;
                    }
                    else
                    {
                        break;
                    }
                }
                prefixLength += count;


                break;
            }
        }
        return prefixLength;
    }

    /**
     * 计算距离
     *
     * @param to
     *
     * @return 距离
     */
    public int getDistance(KademliaId to)
    {
        /**
         * 距离　为　ID_LENGTH - i　为前缀零的个数
         */
        return ID_LENGTH - this.xor(to).getFirstSetBitIndex();
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {

        out.write(this.getBytes());
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        byte[] input = new byte[ID_LENGTH / 8];
        in.readFully(input);
        this.keyBytes = input;
    }

    public String hexRepresentation()
    {
        /* 返回十六进制NodeId */
        BigInteger bi = new BigInteger(1, this.keyBytes);
        return String.format("%0" + (this.keyBytes.length << 1) + "X", bi);
    }

    @Override
    public String toString()
    {
        return this.hexRepresentation();
    }
    
    public String getKadId(){
    	return new String(keyBytes);
    }

}
