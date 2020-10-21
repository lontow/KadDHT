package org.kaddht.Cli_UI;

import org.kaddht.kademlia.JKademliaNode;

import java.util.HashMap;
import java.util.regex.Pattern;

abstract class Exec {
    protected JKademliaNode kad=null;
    abstract  void run();
    abstract void usage();
    public Exec setKad(JKademliaNode kad) {
        this.kad = kad;
        return this;
    }
    abstract void setArgs(String[] args);
}


public class Command{
    private static HashMap<String,Exec> commands=new HashMap<String, Exec>();
    public static void register(String name,Exec exec) {
        commands.put(name, exec);
    }
    public static void parse(String line,JKademliaNode kad) {
        String[] args=Pattern.compile(" ").split(line);
        Exec exec=commands.get(args[0]).setKad(kad);
        exec.setArgs(args);
        exec.run();
    }
}