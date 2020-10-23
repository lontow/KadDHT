package org.kaddht.kademlia.message;

public interface Message extends Streamable
{

    /**
     * 消息的唯一表示符——消息码
     *
     * @return 返回一个消息码
     * */
    public byte code();
}
