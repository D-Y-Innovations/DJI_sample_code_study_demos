package com.dji.sdk.sample.demo.missionoperator;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.demo.camera.FetchMediaView;
import com.dji.sdk.sample.demo.missionmanager.MissionBaseView;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.controller.MyWayPointMissionActivity;
import com.dji.sdk.sample.internal.model.FileInput;
import com.dji.sdk.sample.internal.utils.DownloadHandler;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.media.FetchMediaTaskScheduler;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static dji.keysdk.FlightControllerKey.HOME_LOCATION_LATITUDE;
import static dji.keysdk.FlightControllerKey.HOME_LOCATION_LONGITUDE;

/**
 * Class for waypoint mission.
 */
public class WaypointMissionOperatorView extends MissionBaseView {

    private static final double ONE_METER_OFFSET = 0.00000899322;
    private static final String TAG = WaypointMissionOperatorView.class.getSimpleName();
    private WaypointMissionOperator waypointMissionOperator;
    private WaypointMission mWaypointMission;
    private WaypointMission mission;
    private WaypointMissionOperatorListener listener;
    private WaypointMissionOperatorListener mlistener;
    private final int WAYPOINT_COUNT = 5;

    private MediaManager mediaManager;
    private List<MediaFile> oldMediaFiles;
    private List<MediaFile> latestMediaFiles;
    private FetchMediaTaskScheduler taskScheduler;

    public WaypointMissionOperatorView(Context context) {
        super(context);
    }





    //region Mission Action Demo
    @Override
    public void onClick(View v) {
        if (waypointMissionOperator == null) {
            waypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
        }
        switch (v.getId()) {
            case R.id.btn_simulator:
               if (getFlightController() != null) {
                   flightController.getSimulator()
                                   .start(InitializationData.createInstance(new LocationCoordinate2D(22, 113), 10, 10),
                                          new CommonCallbacks.CompletionCallback() {
                                              @Override
                                              public void onResult(DJIError djiError) {
                                                  showResultToast(djiError);
                                              }
                                          });
               }
                break;
            case R.id.btn_set_maximum_altitude:
                if (getFlightController() != null) {
                    flightController.setMaxFlightHeight(500, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            ToastUtils.setResultToToast(djiError == null ? "Max Flight Height is set to 500m!" : djiError.getDescription());
                        }
                    });
                }
                break;

            case R.id.btn_set_maximum_radius:
                if (getFlightController() != null) {
                    flightController.setMaxFlightRadius(500, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            ToastUtils.setResultToToast(djiError == null ? "Max Flight Radius is set to 500m!" : djiError.getDescription());
                        }
                    });
                }
                break;
            case R.id.btn_load:
                // Example of loading a Mission
                mission = createRandomWaypointMission(WAYPOINT_COUNT, 1);
                DJIError djiError = waypointMissionOperator.loadMission(mission);
                showResultToast(djiError);
                break;

            case R.id.btn_upload:
                // Example of uploading a Mission
                if (WaypointMissionState.READY_TO_RETRY_UPLOAD.equals(waypointMissionOperator.getCurrentState())
                    || WaypointMissionState.READY_TO_UPLOAD.equals(waypointMissionOperator.getCurrentState())) {
                    waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            showResultToast(djiError);
                        }
                    });
                } else {
                    ToastUtils.setResultToToast("Not ready!");
                }
                break;
            case R.id.btn_start:
                // Example of starting a Mission
                if (mission != null) {
                    waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            showResultToast(djiError);
                        }
                    });
                } else {
                    ToastUtils.setResultToToast("Prepare Mission First!");
                }
                break;
            case R.id.btn_stop:
                // Example of stopping a Mission
                waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        showResultToast(djiError);
                    }
                });
                break;
            case R.id.btn_pause:
                // Example of pausing an executing Mission
                waypointMissionOperator.pauseMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        showResultToast(djiError);
                    }
                });
                break;
            case R.id.btn_resume:
                // Example of resuming a paused Mission
                waypointMissionOperator.resumeMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        showResultToast(djiError);
                    }
                });
                break;
            case R.id.btn_download:
                // Example of downloading an executing Mission
                if (WaypointMissionState.EXECUTING.equals(waypointMissionOperator.getCurrentState()) ||
                    WaypointMissionState.EXECUTION_PAUSED.equals(waypointMissionOperator.getCurrentState())) {
                    waypointMissionOperator.downloadMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            showResultToast(djiError);
                        }
                    });
                } else {
                    ToastUtils.setResultToToast("Mission can be downloaded when the mission state is EXECUTING or EXECUTION_PAUSED!");
                }
                break;
                //自己写的加载数据
            case R.id.btn_myLoad:
                WaypointMissionOperatorTool waypointMissionOperatorTool = new WaypointMissionOperatorTool();
                String txt = "{\n" +
                        "   \"startPosition\": {\n" +
                        "        \"absoluteAltitude\": 76.54328155517578,\n" +
                        "        \"gimbalPitch\": 0,\n" +
                        "        \"lat\": 22.589992518798812,\n" +
                        "        \"lon\": 113.98004457818286,\n" +
                        "        \"yaw\": 0\n" +
                        "    },\n" +
                        "    \"gpsList\": [\n" +
                        "        {\n" +
                        "            \"absoluteAltitude\": 76.08706665039062,\n" +
                        "            \"gimbalPitch\": -90.4000015258789,\n" +
                        "            \"lat\": 22.589784056531727,\n" +
                        "            \"lon\": 113.98036856346226,\n" +
                        "            \"shootPhoto\": 0,\n" +
                        "            \"yaw\": 134.20000004768372\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"absoluteAltitude\": 76.23529815673828,\n" +
                        "            \"gimbalPitch\": -180.80000305175781,\n" +
                        "            \"lat\": 22.589431030194767,\n" +
                        "            \"lon\": 113.98069291229368,\n" +
                        "            \"shootPhoto\": 0,\n" +
                        "            \"yaw\": 155.70000004768372\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"absoluteAltitude\": 76.06767272949219,\n" +
                        "            \"gimbalPitch\": -90.9000015258789,\n" +
                        "            \"lat\": 22.58931317982019,\n" +
                        "            \"lon\": 113.98039819095852,\n" +
                        "            \"shootPhoto\": 0,\n" +
                        "            \"yaw\": 154.59999990463257\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"endPosition\": {\n" +
                        "        \"absoluteAltitude\": 76.25504302978516,\n" +
                        "        \"gimbalPitch\": 0.9000015258789,\n" +
                        "        \"lat\": 22.58946024179878,\n" +
                        "        \"lon\": 113.98026310755915,\n" +
                        "        \"yaw\": 126\n" +
                        "    }\n" +
                        "  \n" +
                        "}";
                List<Waypoint> mList = waypointMissionOperatorTool.getDjiWayPointFromMyWayPoints(txt);
                mWaypointMission = waypointMissionOperatorTool.ctreateWaypointMission(mList);
                DJIError mDjiError = waypointMissionOperator.loadMission(mWaypointMission);
                showResultToast(mDjiError);
                break;
                //自己写的上传数据
            case R.id.btn_myUpLoad:
                if (WaypointMissionState.READY_TO_RETRY_UPLOAD.equals(waypointMissionOperator.getCurrentState())
                        || WaypointMissionState.READY_TO_UPLOAD.equals(waypointMissionOperator.getCurrentState())) {
                    waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            String djiErrorString  = djiError == null ? "Action started!" : djiError.getDescription();
                            Log.e("未能成功上传：",djiErrorString);
                        }
                    });
                }else {
                    ToastUtils.setResultToToast("请先准备好路点任务！");
                }
                break;
                //自己写的开始巡航
            case R.id.btn_myStart:
                getCurrentMediaList();
                record();
//                startMission();
                break;
            case R.id.btn_myStop:
                ToastUtils.setResultToToast("停止！");
                waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if(djiError == null){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ToastUtils.setResultToToast("正常停止任务！");
                            stopRecord();
                        }else {
                            ToastUtils.setResultToToast("停止任务报错！"+djiError);
                        }
                    }
                });
                stopRecord();
                break;

            case R.id.btn_myAutoMission:

                //获取当前SD卡中的文件
                oldMediaFiles = mediaManager.getSDCardFileListSnapshot();
                //1.设置相机为录制模式
                if(ModuleVerificationUtil.isCameraModuleAvailable()){
                    DJISampleApplication.getProductInstance().getCamera()
                            .setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(djiError == null){
                                //2.开始录制
                                DJISampleApplication.getProductInstance().getCamera()
                                        .startRecordVideo(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        if(djiError == null){
                                            //3.开始任务
                                            if(mWaypointMission != null){
                                                waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                                                    @Override
                                                    public void onResult(DJIError djiError) {
                                                        if(djiError == null){
                                                            ToastUtils.setResultToToast("成功开始任务！");
                                                        }else {
                                                            ToastUtils.setResultToToast("开始任务失败！"+djiError.getDescription());
                                                        }

                                                    }
                                                });
                                            }else {
                                                ToastUtils.setResultToToast("请先准备好路点任务！");
                                            }
                                        }else {
                                            ToastUtils.setResultToToast("开始录制失败"+djiError.getDescription());
                                        }
                                    }
                                });

                            }
                        }
                    });
                }
                mlistener = new WaypointMissionOperatorListener() {
                    @Override
                    public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

                    }

                    @Override
                    public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {

                    }

                    @Override
                    public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {

                    }

                    @Override
                    public void onExecutionStart() {

                    }

                    @Override
                    public void onExecutionFinish(@Nullable DJIError djiError) {
                        if (ModuleVerificationUtil.isMediaManagerAvailable()) {
                            if (mediaManager == null) {
                                mediaManager = DJISampleApplication.getProductInstance().getCamera().getMediaManager();
                            }
                        }
                        //自动开始下载任务
                        latestMediaFiles = mediaManager.getInternalStorageFileListSnapshot();
                        latestMediaFiles.removeAll(oldMediaFiles);
                        if (taskScheduler == null) {
                            taskScheduler = mediaManager.getScheduler();
                            if (taskScheduler != null && taskScheduler.getState() == FetchMediaTaskScheduler.FetchMediaTaskSchedulerState.SUSPENDED) {
                                taskScheduler.resume(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                        if (djiError != null) {
                                            ToastUtils.setResultToToast("taskScheduler resume failed: " + djiError.getDescription());
                                        }

                                    }
                                });
                            }
                        }
                        DJISampleApplication.getProductInstance()
                                .getCamera()
                                .setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD,
                                        new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {
                                                if (null == djiError) {
                                                   downloadLatestFiles(latestMediaFiles);
                                                }
                                            }
                                        });
                    }
                };
                break;
            default:
                break;
        }
    }

    //获取当前的媒体文件List
    private List<MediaFile> getCurrentMediaList() {
        List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
        return mediaFiles;
    }

    //下载文件通过对比List
    private void downloadMediaFilebyList(List<MediaFile> mediaFiles){
        if (ModuleVerificationUtil.isCameraModuleAvailable()
                && mediaManager != null) {
            final File destDir = new File(Environment.getExternalStorageDirectory().
                    getPath() + "/Dji_Sdk_getFilesByCompareToList/");
            if (mediaFiles != null) {
                for (MediaFile mediaFile : mediaFiles) {
                    mediaFile.fetchFileData(destDir,((mediaFile.getFileName())
                            .replaceAll(".jpg",""))
                            .replaceAll(".mov",""),new DownloadHandler<String>());
                }
            } else {
                ToastUtils.setResultToToast("没有新增的文件！");
            }
        }
    }

    //下载文件通过时间戳的方式

    //停止录制
    private void stopRecord() {
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if(djiError == null){

                                ToastUtils.setResultToToast("正常停止录制！");
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
                            }else{
                                ToastUtils.setResultToToast("停止录制报错！"+djiError);
                            }
                        }
                    });
        }
    }

    private void record() {
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ToastUtils.setResultToToast("SetCameraMode to recordVideo");
                            DJISampleApplication.getProductInstance()
                                    .getCamera()
                                    .startRecordVideo(new CommonCallbacks.CompletionCallback() {
                                        @Override
                                        public void onResult(DJIError djiError) {
                                            if (djiError == null) {
                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                startMission();

                                                ToastUtils.setResultToToast("正常开始录制！");
                                            } else {
                                                ToastUtils.setResultToToast("开始录制异常！" + djiError.getDescription());
                                            }
                                        }

                                    });
                        }

                    });
        }
    }

    private void startMission() {
        if (mWaypointMission != null) {
            waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    showResultToast(djiError);
                }
            });
        } else {
            ToastUtils.setResultToToast("Prepare Mission First!");
        }
    }

    //endregion
    //下载最新的图片视频文件
    public void downloadLatestFiles(List<MediaFile> mediaFiles) {
        if (ModuleVerificationUtil.isCameraModuleAvailable()
                && mediaManager != null) {
            final File destDir = new File(Environment.getExternalStorageDirectory().
                    getPath() + "/Dji_Sdk_LatestFiles/");
            for (final MediaFile mediaFile : mediaFiles) {
                mediaFile.fetchFileData(destDir, ((mediaFile.getFileName())
                        .replace(".jpg",""))
                        .replace(".mov",""), new DownloadHandler<String>() {
                });
            }

        }
    }

    //region View Life-Cycle
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BaseProduct product = DJISampleApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            ToastUtils.setResultToToast("Disconnect");
            return;
        } else {
            if (product instanceof Aircraft) {
                flightController = ((Aircraft) product).getFlightController();
            }

            if (flightController != null) {

                flightController.setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                        homeLatitude = flightControllerState.getHomeLocation().getLatitude();
                        homeLongitude = flightControllerState.getHomeLocation().getLongitude();
                        flightState = flightControllerState.getFlightMode();

                        updateWaypointMissionState();
                    }
                });

            }
        }
        waypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
        setUpListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        tearDownListener();
        if (flightController != null) {
            flightController.getSimulator().stop(null);
            flightController.setStateCallback(null);
        }
        super.onDetachedFromWindow();
    }
    //endregion

    //region Internal Helper Methods
    private FlightController getFlightController(){
        if (flightController == null) {
            BaseProduct product = DJISampleApplication.getProductInstance();
            if (product != null && product instanceof Aircraft) {
                flightController = ((Aircraft) product).getFlightController();
            } else {
                ToastUtils.setResultToToast("Product is disconnected!");
            }
        }

        return flightController;
    }
    private void updateWaypointMissionState(){
        if (waypointMissionOperator != null && waypointMissionOperator.getCurrentState() != null) {
            ToastUtils.setResultToText(FCPushInfoTV,
                                       "home point latitude: "
                                           + homeLatitude
                                           + "\nhome point longitude: "
                                           + homeLongitude
                                           + "\nFlight state: "
                                           + flightState.name()
                                           + "\nCurrent Waypointmission state : "
                                           + waypointMissionOperator.getCurrentState().getName());
        } else {
            ToastUtils.setResultToText(FCPushInfoTV,
                                       "home point latitude: "
                                           + homeLatitude
                                           + "\nhome point longitude: "
                                           + homeLongitude
                                           + "\nFlight state: "
                                           + flightState.name());
        }
    }
    //endregion

    //region Example of Creating a Waypoint Mission

    /**
     * Randomize a WaypointMission
     *
     * @param numberOfWaypoint total number of Waypoint to randomize
     * @param numberOfAction total number of Action to randomize
     */
    private WaypointMission createRandomWaypointMission(int numberOfWaypoint, int numberOfAction) {
        WaypointMission.Builder builder = new WaypointMission.Builder();
        double baseLatitude = 22;
        double baseLongitude = 113;
        Object latitudeValue = KeyManager.getInstance().getValue((FlightControllerKey.create(HOME_LOCATION_LATITUDE)));
        Object longitudeValue =
            KeyManager.getInstance().getValue((FlightControllerKey.create(HOME_LOCATION_LONGITUDE)));
        if (latitudeValue != null && latitudeValue instanceof Double) {
            baseLatitude = (double) latitudeValue;
        }
        if (longitudeValue != null && longitudeValue instanceof Double) {
            baseLongitude = (double) longitudeValue;
        }

        final float baseAltitude = 20.0f;
        builder.autoFlightSpeed(5f);
        builder.maxFlightSpeed(10f);
        builder.setExitMissionOnRCSignalLostEnabled(false);
        builder.finishedAction(WaypointMissionFinishedAction.NO_ACTION);
        builder.flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY);
        builder.headingMode(WaypointMissionHeadingMode.AUTO);
        builder.repeatTimes(1);
        Random randomGenerator = new Random(System.currentTimeMillis());
        List<Waypoint> waypointList = new ArrayList<>();
        for (int i = 0; i < numberOfWaypoint; i++) {
            final double variation = (Math.floor(i / 4) + 1) * 2 * ONE_METER_OFFSET;
            final float variationFloat = (baseAltitude + (i + 1) * 2);
            final Waypoint eachWaypoint = new Waypoint(baseLatitude + variation * Math.pow(-1, i) * Math.pow(0, i % 2),
                                                       baseLongitude + variation * Math.pow(-1, (i + 1)) * Math.pow(0, (i + 1) % 2),
                                                       variationFloat);
            for (int j = 0; j < numberOfAction; j++) {
                final int randomNumber = randomGenerator.nextInt() % 6;
                switch (randomNumber) {
                    case 0:
                        eachWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, 1));
                        break;
                    case 1:
                        eachWaypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                        break;
                    case 2:
                        eachWaypoint.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));
                        eachWaypoint.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
                        break;
                    case 3:
                        eachWaypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH,
                                                                  randomGenerator.nextInt() % 45 - 45));
                        break;
                    case 4:
                        eachWaypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,
                                                                  randomGenerator.nextInt() % 180));
                        break;
                    default:
                        eachWaypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                        break;
                }
            }
            waypointList.add(eachWaypoint);
        }
        builder.waypointList(waypointList).waypointCount(waypointList.size());
        return builder.build();
    }
    //endregion

    //region Not important stuff
    private void setUpListener() {
        // Example of Listener
        listener = new WaypointMissionOperatorListener() {
            @Override
            public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {
                // Example of Download Listener
                if (waypointMissionDownloadEvent.getProgress() != null
                    && waypointMissionDownloadEvent.getProgress().isSummaryDownloaded
                    && waypointMissionDownloadEvent.getProgress().downloadedWaypointIndex == (WAYPOINT_COUNT - 1)) {
                    ToastUtils.setResultToToast("Download successful!");
                }
                updateWaypointMissionState();
            }

            @Override
            public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {
                // Example of Upload Listener
                if (waypointMissionUploadEvent.getProgress() != null
                    && waypointMissionUploadEvent.getProgress().isSummaryUploaded
                    && waypointMissionUploadEvent.getProgress().uploadedWaypointIndex == (WAYPOINT_COUNT - 1)) {
                    ToastUtils.setResultToToast("Upload successful!");
                }
                updateWaypointMissionState();
            }

            @Override
            public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
                // Example of Execution Listener
                Log.d(TAG,
                      (waypointMissionExecutionEvent.getPreviousState() == null
                       ? ""
                       : waypointMissionExecutionEvent.getPreviousState().getName())
                          + ", "
                          + waypointMissionExecutionEvent.getCurrentState().getName()
                          + (waypointMissionExecutionEvent.getProgress() == null
                             ? ""
                             : waypointMissionExecutionEvent.getProgress().targetWaypointIndex));
                updateWaypointMissionState();
            }

            @Override
            public void onExecutionStart() {
                ToastUtils.setResultToToast("Execution started!");
                updateWaypointMissionState();
            }

            @Override
            public void onExecutionFinish(@Nullable DJIError djiError) {
                ToastUtils.setResultToToast("Execution finished!");
                updateWaypointMissionState();
            }
        };

        if (waypointMissionOperator != null && listener != null) {
            // Example of adding listeners
            waypointMissionOperator.addListener(listener);
        }
    }

    private void tearDownListener() {
        if (waypointMissionOperator != null && listener != null) {
            // Example of removing listeners
            waypointMissionOperator.removeListener(listener);
        }
    }

    private void showResultToast(DJIError djiError) {
        ToastUtils.setResultToToast(djiError == null ? "Action started!" : djiError.getDescription());
    }

    @Override
    public int getDescription() {
        return R.string.component_listview_waypoint_mission_operator;
    }

    //endregion
}
