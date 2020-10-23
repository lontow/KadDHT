package org.kaddht.kademlia;

import com.google.gson.annotations.SerializedName;

import java.io.File;

/**
 * A set of Kademlia configuration parameters. Default values are
 * supplied and can be changed by the application as necessary.
 *
 */
public class DefaultConfiguration implements KadConfiguration
{

    @SerializedName("restore_interval")
    private   long RESTORE_INTERVAL = 60 * 1000; // in milliseconds
    @SerializedName("response_timeout")
    private   long RESPONSE_TIMEOUT = 2000;
    @SerializedName("operation_timeout")
    private   long OPERATION_TIMEOUT = 2000;
    @SerializedName("concurrency")
    private   int CONCURRENCY = 10;
    private final static int K = 5;
    @SerializedName("rcsize")
    private   int RCSIZE = 3;
    @SerializedName("stale")
    private   int STALE = 1;
    @SerializedName("default_folder")
    private   String LOCAL_FOLDER = "test";
    public   String dirPath=System.getProperty("user.home") + File.separator + LOCAL_FOLDER;
    
    private final static boolean IS_TESTING = true;
    private static DefaultConfiguration config=null;
    public static synchronized DefaultConfiguration getInstance(){
        if(config==null){
         //   config = new DefaultConfiguration();
        }
        return config;

    }
    /**
     * Default constructor to support Gson Serialization
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
        /* Setup the main storage folder if it doesn't exist */
        String path = System.getProperty("user.home") + File.separator + LOCAL_FOLDER;
        File folder = new File(path);
        if (!folder.isDirectory())
        {
            folder.mkdir();
        }

        /* Setup subfolder for this owner if it doesn't exist */
        File ownerFolder = new File(folder + File.separator + ownerId);
        if (!ownerFolder.isDirectory())
        {
            ownerFolder.mkdir();
        }

        /* Return the path */

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
        return dirPath;
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
                ", dirPath='" + dirPath + '\'' +
                ", IS_TESTING=" + IS_TESTING +
                '}';
    }

    public static void setConfig(DefaultConfiguration conf){
        config=conf;
    }
}
