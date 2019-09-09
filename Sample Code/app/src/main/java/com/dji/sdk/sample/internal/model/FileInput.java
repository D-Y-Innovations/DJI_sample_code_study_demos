package com.dji.sdk.sample.internal.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 读取文件内容
 */
public class FileInput {
    private String fileName;

    FileInput(){

    }
   public FileInput(String fileName){
        this.fileName = fileName;
    }
    //将文件读取为字符串
    public String getFileOfString() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        FileReader fr = new FileReader(fileName);
        BufferedReader bf = new BufferedReader(fr);
        String str;
        // 按行读取字符串
        while ((str = bf.readLine()) != null) {
            stringBuffer.append(str);
        }
        bf.close();
        fr.close();
        return stringBuffer.toString();
    }
}
