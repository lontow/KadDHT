package org.kaddht.kademlia.util;

import com.google.gson.Gson;
import org.kaddht.kademlia.DefaultConfiguration;

import java.io.*;

class TxtFile{
    String filename;
    String data;
    public TxtFile(String name,String data){
        this.filename=name;
        this.data=data;
    }
}
public class LocalFileReader {
    public static String read(String filename)  {
        String line = "",content="",txt="";
        try {
            String txtname =DefaultConfiguration.dirPath+File.separator+filename;
            File file = new File(DefaultConfiguration.dirPath+File.separator+filename);
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"), 5 * 1024 * 1024);// 用5M的缓冲读取文本文件


            while ((line=reader.readLine())!=null) {
                content += line;
            }
            txt = new Gson().toJson(new TxtFile(txtname,content));
        }catch (IOException e){

        }
        System.out.println("read:"+txt);
        return txt;

    }


    public LocalFileReader() throws FileNotFoundException, UnsupportedEncodingException {
    }
}
