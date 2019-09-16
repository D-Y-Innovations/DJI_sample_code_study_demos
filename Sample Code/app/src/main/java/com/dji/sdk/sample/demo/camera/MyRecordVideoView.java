package com.dji.sdk.sample.demo.camera;

import android.content.Context;
import android.os.Environment;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.BaseThreeBtnView;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.PlaybackManager;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;


public class MyRecordVideoView extends BaseThreeBtnView{
//
    private PlaybackManager playbackManager;
    private MediaManager mediaManager;

    private Timer timer = new Timer();
    private long timeCounter = 0;
    private long hours = 0;
    private long minutes = 0;
    private long seconds = 0;
    private String time = "";

    public MyRecordVideoView(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    ToastUtils.setResultToToast("SetCameraMode to recordVideo");
                                }
                            });
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    ToastUtils.setResultToToast("SetCameraMode to shootPhoto");
                                }
                            });
        }
    }

    @Override
    protected int getLeftBtnTextResourceId() {
        return R.string.my_start_record;
    }

    @Override
    protected int getRightBtnTextResourceId() {

        return R.string.my_download_record;
//        return DISABLE;
    }

    @Override
    protected int getMiddleBtnTextResourceId() {
        return R.string.my_stop_record;
    }

    @Override
    protected int getDescriptionResourceId() {
        return R.string.record_video_initial_time;
    }

    @Override
    protected void handleLeftBtnClick() {

        changeDescription("00:00:00");
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
                            DJISampleApplication.getProductInstance()
                                    .getCamera()
                                    .startRecordVideo(new CommonCallbacks.CompletionCallback() {
                                        @Override
                                        public void onResult(DJIError djiError) {
                            //success so, start recording
                            if (null == djiError) {
                                ToastUtils.setResultToToast("Start record");
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        timeCounter = timeCounter + 1;
                                        hours = TimeUnit.MILLISECONDS.toHours(timeCounter);
                                        minutes =
                                                TimeUnit.MILLISECONDS.toMinutes(timeCounter) - (hours * 60);
                                        seconds = TimeUnit.MILLISECONDS.toSeconds(timeCounter) - ((hours
                                                * 60
                                                * 60) + (minutes * 60));
                                        time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                                        changeDescription(time);
                                    }
                                }, 0, 1);
                            }
                        }
                    });
        }
    }

    @Override
    protected void handleMiddleBtnClick() {

        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            ToastUtils.setResultToToast("StopRecord");
                            changeDescription("00:00:00");
                            timer.cancel();
                            timeCounter = 0;
                        }
                    });
        }
    }

    @Override
    protected void handleRightBtnClick() {

        if(ModuleVerificationUtil.isPlaybackAvailable()
                && mediaManager != null){
            mediaManager.deleteFiles(mediaManager.getSDCardFileListSnapshot(), new CommonCallbacks.CompletionCallbackWithTwoParam<List<MediaFile>, DJICameraError>() {
                @Override
                public void onSuccess(List<MediaFile> mediaFiles, DJICameraError djiCameraError) {
                    ToastUtils.setResultToToast("重置SD卡成功！"+djiCameraError);
                }

                @Override
                public void onFailure(DJIError djiError) {
                    ToastUtils.setResultToToast("重置SD卡失败！"+djiError);
                }
            });
        }
//       Download Button
//        if (ModuleVerificationUtil.isPlaybackAvailable()) {
//            playbackManager = DJISampleApplication.getProductInstance().getCamera().getPlaybackManager();
//
//            File destDir = new File(Environment.getExternalStorageDirectory().
//                    getPath() + "/My_Dji_Sdk_Test/");
//            playbackManager.downloadSelectedFiles(destDir, new PlaybackManager.FileDownloadCallback() {
//
//                @Override
//                public void onStart() {
//
//                    changeDescription("Start");
//                }
//
//                @Override
//                public void onEnd() {
//
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    changeDescription(e.toString());
//                }
//
//                @Override
//                public void onProgressUpdate(int progress) {
//                    changeDescription("Progress: " + progress);
//                }
//            });
//        }
    }

    @Override
    public int getDescription() {
        return R.string.camera_listview_record_video;
    }
}
