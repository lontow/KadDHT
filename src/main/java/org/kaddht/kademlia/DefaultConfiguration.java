package org.kaddht.kademlia;

import com.google.gson.annotations.SerializedName;

import java.io.File;

/**
 * 一组Kademlia配置参数
 * 具有默认值，同时可以根据需要由应用程序进行更改
 *
 */
public class DefaultConfiguration implements KadConfiguration
{

    @SerializedName("restore_interval")
    private   static long RESTORE_INTERVAL = 60 * 1000;
        // 以毫秒为单位

    @SerializedName("response_timeout")
    private   static long RESPONSE_TIMEOUT = 2000;
    @SerializedName("operation_timeout")
    private   static long OPERATION_TIMEOUT = 2000;
    @SerializedName("concurrency")
    private   static int CONCURRENCY = 10;
    private final static int K = 5;
    @SerializedName("rcsize")
    private   static int RCSIZE = 3;
    @SerializedName("stale")
    private   static int STALE = 1;
    @SerializedName("default_folder")
    private   String LOCAL_FOLDER = "test";

    private final static boolean IS_TESTING = true;
    public static boolean savestatus=false;
    private static DefaultConfiguration config=null;
    public static synchronized DefaultConfiguration getInstance(){
        if(config==null){
           config = new DefaultConfiguration();
        }
        return config;

    }
    /**
     * 支持 Gson 序列化的默认构造函数
     */
    public DefaultConfiguration()
    {

    }

    @Override
    public long restoreInterval()
    {
        return RESTORE_INTERVAL;
    }

    @Override
    public long responseTimeout()
    {
        return RESPONSE_TIMEOUT;
    }

    @Override
    public long operationTimeout()
    {
        return OPERATION_TIMEOUT;
    }

    @Override
    public int maxConcurrentMessagesTransiting()
    {
        return CONCURRENCY;
    }

    @Override
    public int k()
    {
        return K;
    }

    @Override
    public int replacementCacheSize()
    {
        return RCSIZE;
    }

    @Override
    public int stale()
    {
        return STALE;
    }

    @Override
    public String getNodeDataFolder(String ownerId)
    {
        // 设置主存储文件夹（如果其不存在）
        String path = DefaultConfiguration.getInstance().getDirPath();
        //System.out.println("getNodeDataFolder:"+path);
        File folder = new File(path);
        if (!folder.isDirectory())
        {
            folder.mkdir();
        }

        // 如果此所有者不存在，设置子文件夹
        File ownerFolder = new File(folder + File.separator + ownerId);
        if (!ownerFolder.isDirectory())
        {
            ownerFolder.mkdir();
        }

        // 返回路径

        return ownerFolder.toString();
    }

    @Override
    public boolean isTesting()
    {
        return IS_TESTING;
    }

    public int getCONCURRENCY() {
        return CONCURRENCY;
    }

    public long getRESTORE_INTERVAL() {
        return RESTORE_INTERVAL;
    }

    public long getRESPONSE_TIMEOUT() {
        return RESPONSE_TIMEOUT;
    }

    public long getOPERATION_TIMEOUT() {
        return OPERATION_TIMEOUT;
    }

    public static int getK() {
        return K;
    }

    public int getRCSIZE() {
        return RCSIZE;
    }

    public int getSTALE() {
        return STALE;
    }

    public String getLOCAL_FOLDER() {
        return LOCAL_FOLDER;
    }

    public String getDirPath() {
        return System.getProperty("user.home") + File.separator + LOCAL_FOLDER;
    }

    public static boolean isIsTesting() {
        return IS_TESTING;
    }

    @Override
    public String toString() {
        return "DefaultConfiguration{" +
                "RESTORE_INTERVAL=" + RESTORE_INTERVAL +
                ", RESPONSE_TIMEOUT=" + RESPONSE_TIMEOUT +
                ", OPERATION_TIMEOUT=" + OPERATION_TIMEOUT +
                ", CONCURRENCY=" + CONCURRENCY +
                ", K=" + K +
                ", RCSIZE=" + RCSIZE +
                ", STALE=" + STALE +
                ", LOCAL_FOLDER='" + LOCAL_FOLDER + '\'' +
                ", "+ '\'' +
                ", IS_TESTING=" + IS_TESTING +
                '}';
    }

    public static void setConfig(DefaultConfiguration conf){
        config=conf;
    }
}
