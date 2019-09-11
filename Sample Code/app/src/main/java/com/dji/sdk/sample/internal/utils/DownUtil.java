package com.dji.sdk.sample.internal.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.media.MediaFile;

public class DownUtil {
    public void download(final MediaFile mediaFile){
        mediaFile.fetchPreview(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError == null){
                    //获取到当前的预览文件
                    Bitmap bitmap = mediaFile.getPreview();
                    String photoName = mediaFile.getFileName();
                    saveBitmap(bitmap,photoName);
                }else{
                    Log.e("myDownloadFile","图片下载回调错误！");
                }
            }
        });
    }

    /**
     * 将图片保存到
     * @param bitmap
     * @param strFilename
     */
    public void  saveBitmap(Bitmap bitmap,String strFilename){
        File  file = new File(Environment.getExternalStorageDirectory().
                getPath()+"My_SDK_Test",strFilename);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            //将Bitmap解析为流
            bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
