package org.kaddht.Cli_UI;

/**
 * @author 刘朕龙
 * @create 2020-10-20
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.kaddht.kademlia.DefaultConfiguration;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.exceptions.RoutingException;
import org.kaddht.kademlia.node.KademliaId;
import org.kaddht.kademlia.node.Node;

public class Main {
    private static KadPeer kad1=null;
    static void  parserConfig(String path) {
        System.out.println(path);
        File configfile = new File(path);
        Gson gson = new Gson();
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(configfile));
            Config config=gson.fromJson(jsonReader, Config.class);
             kad1 = new KadPeer(config.localnode.ownerid, new KademliaId(config.localnode.kadid), config.localnode.udpport);
            System.out.println("Created Node Kad : ");
            System.out.println( " \tat "+kad1.getNode().getSocketAddress());
            System.out.println( " \tkadid: "+kad1.getNode().getNodeId().getKadId());
            DefaultConfiguration.setConfig(config.system);
            System.out.println(" \tdefault directory:" + DefaultConfiguration.getInstance().getDirPath());
            System.out.println(config.system);


            if(config.tracker!=null){
                Node tracker=new Node(new KademliaId(config.tracker.kadid),
                        InetAddress.getByName(config.tracker.address),
                        config.tracker.udpport);
                kad1.bootstrap(tracker);
                System.out.println("Connected to tracker : " + tracker.getSocketAddress());
                // 输出kad1.getRoutingTable()
            }
        } catch (IOException e) {
            if(e instanceof FileNotFoundException)
            System.out.println("configFile is not exists");
            else if(e instanceof RoutingException)
                e.printStackTrace();
            System.exit(0);
        }

    }
    public static void main(String[] args) {
        Option help = new Option("help", "print this message");
        Option config = Option.builder("config")
                .hasArg(true)
                .required()
                .argName("file")
                .desc("config file")
                .build();
        Options options = new Options();
        options.addOption(help);
        options.addOption(config);

        CommandLineParser parser = new DefaultParser();
        try {
            // 解析命令行参数
            CommandLine line = parser.parse( options, args );
            if(line.hasOption("help")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "kadDHT", options ,true);
            }
            if (line.hasOption("config")) {
                String path=line.getOptionValue("config");
                parserConfig(path);
            }
        }
        catch( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "kadDHT", options ,true);
            System.exit(0);
        }

        try {
            new Shell(kad1).run();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}