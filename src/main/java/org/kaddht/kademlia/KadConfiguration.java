package org.kaddht.kademlia;

/**
 * 定义KadConfiguration对象的接口
 *
 * @author 张文令
 * @since 20201010
 */
public interface KadConfiguration
{

    /**
     * @return Interval 执行 RestoreOperations 的毫秒数
     */
    public long restoreInterval();

    /**
     * 如果在此期间（以毫秒为单位）未收到来自节点的答复，则认为该节点无响应
     *
     * @return 认为节点无响应所需的时间
     */
    public long responseTimeout();

    /**
     * @return 执行操作的最大毫秒数
     */
    public long operationTimeout();

    /**
     * @return 传输中的最大并发消息数
     */
    public int maxConcurrentMessagesTransiting();

    /**
     * @return K-Value used throughout Kademlia
     */
    public int k();

    /**
     * @return 替换缓存的大小
     */
    public int replacementCacheSize();

    /**
     * @return 在实际删除某个节点之前，可以将其标记为过时的次数
     */
    public int stale();

    /**
     * 创建要在其中存储此节点数据的文件夹
     *
     * @param ownerId
     *
     * @return The folder path
     */
    public String getNodeDataFolder(String ownerId);

    /**
     * @return 返回我们是否在测试系统中
     */
    public boolean isTesting();
}
