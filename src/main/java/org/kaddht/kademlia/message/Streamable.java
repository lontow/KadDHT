package org.kaddht.kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 相较于　serialize 更节省空间
 *
 * */
public interface Streamable
{

    /**
     * 向输出流写流式对象的内部状态
     *
     *
     * @param out
     *
     * @throws java.io.IOException
     */
    public void toStream(DataOutputStream out) throws IOException;

    /**
     * 从输入流中读取流式对象的内部状态
     *
     * @param out
     *
     * @throws java.io.IOException
     */
    public void fromStream(DataInputStream out) throws IOException;
}
