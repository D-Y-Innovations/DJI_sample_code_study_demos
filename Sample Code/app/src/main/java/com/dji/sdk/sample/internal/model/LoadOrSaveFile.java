package com.dji.sdk.sample.internal.model;

import android.content.Context;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

public class LoadOrSaveFile {
    /**
     * 利用Android提供的方法
     * 文件加载
     * @param fileName
     * @return
     */
    public String loadByAndroid(String fileName){
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder contentOfFile = new StringBuilder();
        try{
            in = DJISampleApplication.getContext().openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(in));
            String line ="";
            while((line = reader.readLine() )!= null){
                contentOfFile.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return contentOfFile.toString();
    }

    /**
     * Android提供的方法
     * 将数据保存到文件中
     * @param fileName
     * @param data
     */
    public void saveByAndroid(String fileName, String data){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try{
            out = DJISampleApplication.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(writer != null){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 通过行的形式读取文件
     * @param fileName
     */
    public String readFileByLine(String fileName){
        File file = new File(fileName);
        InputStream inputStream = null;
        Reader reader = null;
        BufferedReader bufferedReader = null;
        StringBuffer contentOfFile = new StringBuffer();
        try {
            inputStream = new FileInputStream(file);
            reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
            String line = "";
            if ((line = bufferedReader.readLine())!=null){
                contentOfFile.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if (reader != null){
                    reader.close();
                }
                if (inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contentOfFile.toString();
    }

    /**
     * 按字节读取文件
     * @param fileName
     * @return
     */
    public String readFileByByte(String fileName){
        File file = new File(fileName);
        InputStream inputStream = null;
        StringBuffer contentOfFile = new StringBuffer();
        try{
            inputStream = new FileInputStream(file);
            byte[] tempbyte = new byte[1024];
            int index = 0;
            while (-1 != (index = inputStream.read())){
                contentOfFile.append(index);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return contentOfFile.toString();
    }

    //将字符写入文件
    public void saveChar(String fileName, String data){

        try{
            FileWriter fw = new FileWriter(fileName);
            fw.write(data);
            fw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
