package org.kaddht.Cli_UI;

import java.io.IOException;


import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.kaddht.kademlia.JKademliaNode;
import org.kaddht.kademlia.dht.JKademliaStorageEntry;
import org.kaddht.kademlia.simulations.DHTContentImpl;
import org.kaddht.kademlia.util.LocalFileReader;


class Exit extends Exec{


    public void run() {
        // TODO Auto-generated method stub
        System.exit(0);
    }

    public void usage() {
        // TODO Auto-generated method stub
        System.out.println("exit");

    }

    @Override
    void setArgs(String[] args) {

    }


}

class Put extends Exec{

    String content=null;
    public void run()  {
        // TODO Auto-generated method stub
        if(content==null) return;
        DHTContentImpl c = new DHTContentImpl(kad.getOwnerId(),content);
        try {
            kad.put(c);
        }catch (IOException e){

        }

    }


    public void usage() {
        // TODO Auto-generated method stub
        System.out.println("put <filename>");
    }

    @Override
    void setArgs(String[] args) {
        if(args.length!=2) {
            usage();
            return;
        }
        content=LocalFileReader.read(args[1]);
    }



}

class Get extends Exec{

    private String key=null;
    public void run() {
        // TODO Auto-generated method stub
        if(key==null) return;
        try {
            JKademliaStorageEntry entry=kad.get(key);
            new DHTContentImpl().fromSerializedForm(entry.getContent());
        }catch (Exception e) {
        }
    }


    public void usage() {
        // TODO Auto-generated method stub
        System.out.println("get <key>");
    }

    @Override
    void setArgs(String[] args) {
        if(args.length!=2) {
            usage();
            return;
        }
        key=LocalFileReader.read(args[1]);
    }



}
class ShowRoute extends Exec{

    public void run() {
        // TODO Auto-generated method stub
        System.out.println(kad.getRoutingTable());
    }


    public void usage() {
        // TODO Auto-generated method stub
        System.out.println("showroute");
    }

    @Override
    void setArgs(String[] args) {

    }


}


public class Shell {

    private JKademliaNode kad=null;
    public Shell(JKademliaNode kad) {
        this.kad=kad;
    }

    private void init(){
        Command.register("exit", new Exit());
        Command.register("showroute", new ShowRoute());
        Command.register("put", new Put());
        Command.register("get", new Get());
    }
    public void run() throws IOException {
        init();
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        String prompt = "Kad> ";
        while (true) {
            String line;
            try {
                line = lineReader.readLine(prompt);
                //System.out.println(line);
                Command.parse(line,kad);
            } catch (UserInterruptException e) {
                // Do nothing
            } catch (EndOfFileException e) {
                System.out.println("\nBye.");
                return;
            }
        }
    }
}
