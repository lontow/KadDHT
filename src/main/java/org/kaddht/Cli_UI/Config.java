package org.kaddht.Cli_UI;

/**
 * @author 刘朕龙
 * @create 2020-10-20
 */

import com.google.gson.annotations.SerializedName;
import org.kaddht.kademlia.DefaultConfiguration;

class Node{
    String ownerid;
    String kadid;
    int udpport;
}
class Tracker{
    String kadid;
    String address;
    int udpport;
}
public class Config {
    @SerializedName("localnode")
    Node localnode;
    @SerializedName("tracker")
    Tracker tracker;
    @SerializedName("system")
    DefaultConfiguration system;
}
