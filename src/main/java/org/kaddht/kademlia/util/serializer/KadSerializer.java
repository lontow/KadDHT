package org.kaddht.kademlia.util.serializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 序列化
 *
 * @author Lontow
 * @since 20201020
 */
public interface KadSerializer<T>
{

    /**
     *
     * @param data
     * @param out
     *
     * @throws java.io.IOException
     */
    public void write(T data, DataOutputStream out) throws IOException;

    /**
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public T read(DataInputStream in) throws IOException, ClassNotFoundException;
}
