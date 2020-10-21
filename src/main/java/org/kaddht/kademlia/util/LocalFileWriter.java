package org.kaddht.kademlia.util;

import com.google.gson.Gson;
import org.kaddht.kademlia.DefaultConfiguration;

import java.io.*;

public class LocalFileWriter {
    public static void write(String content){
        TxtFile txtFile=new Gson().fromJson(content,TxtFile.class);
        try {
            String txtname = DefaultConfiguration.dirPath+ File.separator+txtFile.filename;
            File file = new File(txtname);
            BufferedOutputStream fis = new BufferedOutputStream(new FileOutputStream(file));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fis, "utf-8"), 5 * 1024 * 1024);// 用5M的缓冲读取文本文件

            writer.write(txtFile.data);
            System.out.println("LocalWriter:"+txtFile.data);
        }catch (IOException e){

        }
    }
}
