package com.dji.sdk.sample.demo.missionoperator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;

public class AutoWaypointMission extends RelativeLayout implements View.OnClickListener {
    private Button autoMissionBtn;
    public AutoWaypointMission(Context context) {
        super(context);
        initUI(context);
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

    //加载UI
    private void initUI(Context context){
        setClickable(true);
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_automission,this,true);

        autoMissionBtn = findViewById(R.id.autoBtn);
        autoMissionBtn.setOnClickListener( this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.autoBtn:

                break;
                default:
                    break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
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
}
