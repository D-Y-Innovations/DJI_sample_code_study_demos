package com.dji.sdk.sample.demo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.DownUtil;
import com.dji.sdk.sample.internal.utils.DownloadHandler;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.BaseThreeBtnView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

/**
 * Class for shooting single photo.
 */
public class MyShootSinglePhotoView extends BaseThreeBtnView {

    private MediaFile mediaFile;
    private MediaManager mediaManager;
    private Context context;

    public MyShootSinglePhotoView(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Every commands relative to the shooting photos are only allowed executed in shootphoto work
     * mode.
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.v("Attached To Window", "onAttachedToWindow");

        if (isModuleAvailable()) {

            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {

                                }
                            });
        }
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            if (ModuleVerificationUtil.isMediaManagerAvailable()) {
                if (mediaManager == null) {
                    mediaManager = DJISampleApplication.getProductInstance().getCamera().getMediaManager();
                }
            }
        }
    }

    private boolean isModuleAvailable() {
        return (null != DJISampleApplication.getProductInstance()) && (null != DJISampleApplication.getProductInstance()
                .getCamera());
    }

    @Override
    protected int getLeftBtnTextResourceId() {
        return R.string.my_shoot_photo_button;
    }

    @Override
    protected int getDescriptionResourceId() {
        return getDescription();
    }

    //拍照
    @Override
    protected void handleLeftBtnClick() {
        shootPhoto();

    }

    public void shootPhoto() {
        //Shoot Photo Button
        if (isModuleAvailable()) {
            post(new Runnable() {
                @Override
                public void run() {
                    middleBtn.setEnabled(false);
                }
            });

            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .startShootPhoto(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (null == djiError) {
                                ToastUtils.setResultToToast(getContext().getString(R.string.success));
                            } else {
                                ToastUtils.setResultToToast(djiError.getDescription());
                            }
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    middleBtn.setEnabled(true);
                                }
                            });
                        }
                    });
        }
    }

    @Override
    protected void handleMiddleBtnClick() {

    }

    //下载SD卡上的所有照片
    @Override
    protected void handleRightBtnClick() {
        if (ModuleVerificationUtil.isCameraModuleAvailable()
                && mediaFile != null
                && mediaManager != null){

                final File destDir = new File(Environment.getExternalStorageDirectory().
                        getPath() + "/Dji_Sdk_MyTest/");

                List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
                int i = 0;
                for(final MediaFile mediaFile : mediaFiles){
                    mediaFile.fetchFileData(destDir,mediaFile.getFileName(),new DownloadHandler<String>());
                    i++;
                    ToastUtils.setResultToToast("下载完成第"+i+"份(图片)/（视频）！");
                }
        }
    }

    @Override
    protected int getRightBtnTextResourceId() {
        return R.string.my_download_photo_button;
    }

    @Override
    protected int getMiddleBtnTextResourceId() {
        return DISABLE;
    }

    @Override
    public int getDescription() {
        return R.string.camera_listview_shoot_single_photo;
    }
}
