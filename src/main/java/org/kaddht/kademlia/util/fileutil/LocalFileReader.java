package org.kaddht.kademlia.util.fileutil;

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
            String txtname =DefaultConfiguration.getInstance().getDirPath()+File.separator+filename;
            File file = new File(DefaultConfiguration.getInstance().getDirPath()+File.separator+filename);
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"), 5 * 1024 * 1024);// 用5M的缓冲读取文本文件


            while ((line=reader.readLine())!=null) {
                content += line;
                content += "\n";
                    //手动添加换行
            }
            reader.close();
            txt = new Gson().toJson(new TxtFile(filename,content));
        }catch (IOException e){

        }
        System.out.println("read:"+txt);
        return txt;

    }


    public LocalFileReader() throws FileNotFoundException, UnsupportedEncodingException {
    }
}
