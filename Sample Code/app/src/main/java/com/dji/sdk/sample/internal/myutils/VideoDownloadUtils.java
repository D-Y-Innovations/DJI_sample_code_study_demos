package com.dji.sdk.sample.internal.myutils;

import android.os.Environment;
import android.util.Log;

import com.dji.sdk.sample.demo.camera.VideoFeederView;

import java.io.File;

import dji.common.error.DJIError;
import dji.sdk.media.AudioMediaFile;
import dji.sdk.media.DownloadListener;

public class VideoDownloadUtils {

    private static final String TAG = VideoDownloadUtils.class.getSimpleName();

    //从SD卡中获取全分辨率数据
    public void saveFetchFileData(AudioMediaFile audioMediaFile){
        File file = new File(Environment.getDataDirectory()+"my_sample_code_test");
        audioMediaFile.fetchFileData(file, audioMediaFile.getFileName(), new DownloadListener<String>() {
            @Override
            public void onStart() {
                Log.i(TAG,"开始下载");

            }

            @Override
            public void onRateUpdate(long l, long l1, long l2) {

            }

            @Override
            public void onProgress(long l, long l1) {

            }

            @Override
            public void onSuccess(String s) {
                Log.i(TAG,"下载成功");
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }


}
