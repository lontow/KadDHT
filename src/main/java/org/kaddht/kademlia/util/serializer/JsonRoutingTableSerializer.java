package org.kaddht.kademlia.util.serializer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.kaddht.kademlia.routing.KadRoutingTable;
import java.lang.reflect.Type;
import java.util.List;
import org.kaddht.kademlia.KadConfiguration;
import org.kaddht.kademlia.routing.Contact;
import org.kaddht.kademlia.routing.KademliaRoutingTable;

/**
 *Kad 序列化
 * @author Lontow
 *
 * @since 20201020
 */
public class JsonRoutingTableSerializer implements KadSerializer<KademliaRoutingTable>
{

    private final Gson gson;

    Type contactCollectionType = new TypeToken<List<Contact>>()
    {
    }.getType();

    private final KadConfiguration config;

    
    {
        gson = new Gson();
    }


    public JsonRoutingTableSerializer(KadConfiguration config)
    {
        this.config = config;
    }

    @Override
    public void write(KademliaRoutingTable data, DataOutputStream out) throws IOException
    {
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out)))
        {
            writer.beginArray();

            gson.toJson(data, KadRoutingTable.class, writer);


            gson.toJson(data.getAllContacts(), contactCollectionType, writer);

            writer.endArray();
        }
    }

    @Override
    public KademliaRoutingTable read(DataInputStream in) throws IOException, ClassNotFoundException
    {
        try (DataInputStream din = new DataInputStream(in);
                JsonReader reader = new JsonReader(new InputStreamReader(in)))
        {
            reader.beginArray();


            KademliaRoutingTable tbl = gson.fromJson(reader, KadRoutingTable.class);
            tbl.setConfiguration(config);
            

            List<Contact> contacts = gson.fromJson(reader, contactCollectionType);
            tbl.initialize();

            for (Contact c : contacts)
            {
                tbl.insert(c);
            }

            reader.endArray();
            return tbl;
        }
    }
}
