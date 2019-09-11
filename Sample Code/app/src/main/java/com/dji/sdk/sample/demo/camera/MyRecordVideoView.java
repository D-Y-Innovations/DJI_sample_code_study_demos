package com.dji.sdk.sample.demo.camera;

import android.content.Context;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.BaseThreeBtnView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;

public class MyRecordViedoView extends BaseThreeBtnView {

    private Timer timer = new Timer();
    private long timeCounter = 0;
    private long hours = 0;
    private long minutes = 0;
    private long seconds = 0;
    private String time = "";

    public MyRecordViedoView(Context context) {
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
    protected int getMiddleBtnTextResourceId() {
        return R.string.stop_record;
    }

    @Override
    protected int getLeftBtnTextResourceId() {
        return R.string.start_record;
    }

    @Override
    protected int getRightBtnTextResourceId() {
        return R.string.fetch_media_view_fetch_media;
    }

    @Override
    protected int getDescriptionResourceId() {
        return 0;
    }

    //停止录制
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

    //开始录制
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
    protected void handleRightBtnClick() {

    }

    @Override
    public int getDescription() {
        return 0;
    }
}
