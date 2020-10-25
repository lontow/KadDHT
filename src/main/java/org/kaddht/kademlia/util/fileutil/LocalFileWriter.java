package org.kaddht.kademlia.util.fileutil;

import com.google.gson.Gson;
import org.kaddht.kademlia.DefaultConfiguration;

import java.io.*;

public class LocalFileWriter {
    public static void write(String content){
        System.out.println(content);
        TxtFile txtFile=new Gson().fromJson(content,TxtFile.class);
        try {
            String txtname = DefaultConfiguration.getInstance().getDirPath()+ File.separator+txtFile.filename;
            File file = new File(txtname);
            file.createNewFile();
            BufferedOutputStream fis = new BufferedOutputStream(new FileOutputStream(file));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fis, "utf-8"), 5 * 1024 * 1024);// 用5M的缓冲读取文本文
            writer.write(txtFile.data);
            writer.close();
            System.out.println("LocalWriter:\n"+txtFile.data);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
