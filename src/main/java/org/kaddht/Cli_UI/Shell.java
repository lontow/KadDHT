package org.kaddht.Cli_UI;

/**
 * @author 刘朕龙
 * @create 2020-10-20
 */

import java.io.IOException;


import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.kaddht.kademlia.KadPeer;
import org.kaddht.kademlia.dht.KadStorageEntry;
import org.kaddht.kademlia.dht.DHTContentImpl;
import org.kaddht.kademlia.util.fileutil.LocalFileReader;
import org.kaddht.kademlia.util.fileutil.LocalFileWriter;


class Exit extends Exec{

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.exit(0);
    }

    @Override
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

    @Override
    public void run()  {
        // TODO Auto-generated method stub
        if(content==null) {
            return;
        }
        DHTContentImpl c = new DHTContentImpl(kad.getOwnerId(),content);
        try {
            kad.put(c);
        }catch (IOException e){

        }

    }


    @Override
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

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if(key==null) {
            return;
        }
        try {
            KadStorageEntry entry=kad.get(key);
            String content= new DHTContentImpl().fromSerializedForm(entry.getContent()).getData();
            LocalFileWriter.write(content);
        }catch (Exception e) {
        }
    }


    @Override
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
        key=args[1];
    }



}
class ShowRoute extends Exec{

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.println(kad.getRoutingTable());
    }


    @Override
    public void usage() {
        // TODO Auto-generated method stub
        System.out.println("showroute");
    }

    @Override
    void setArgs(String[] args) {

    }


}


public class Shell {

    private KadPeer kad=null;
    public Shell(KadPeer kad) {
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
