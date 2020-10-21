package org.kaddht.Cli_UI;


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
    Node localnode;
    Tracker tracker;
}
