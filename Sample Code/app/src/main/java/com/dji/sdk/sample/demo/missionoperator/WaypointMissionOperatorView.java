package com.dji.sdk.sample.demo.missionoperator;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.demo.missionmanager.MissionBaseView;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static dji.keysdk.FlightControllerKey.HOME_LOCATION_LATITUDE;
import static dji.keysdk.FlightControllerKey.HOME_LOCATION_LONGITUDE;

/**
 * Class for waypoint mission.
 */
public class WaypointMissionOperatorView extends MissionBaseView {

    private static final double ONE_METER_OFFSET = 0.00000899322;
    private static final String TAG = WaypointMissionOperatorView.class.getSimpleName();
    private static final String FUTURETESTTAG = "future_test";
    private static final String DJIERROR_NULL = "djiError_null";
    private static final String DJIERROR_UNNULL = "djiError_unNull";
    private static final SettingsDefinitions.CameraMode A = SettingsDefinitions.CameraMode.RECORD_VIDEO ;
    private WaypointMissionOperator waypointMissionOperator;
    private WaypointMission mWaypointMission;
    private WaypointMission mission;
    private WaypointMissionOperatorListener listener;
    private WaypointMissionOperatorListener mlistener;
    private final int WAYPOINT_COUNT = 5;

    private MediaManager mediaManager;
    private List<MediaFile> oldMediaFiles ;
    private  List<MediaFile> latestMediaFiles;
    private FetchMediaTaskScheduler taskScheduler;
    //获取当前文件最大的Index
    private int preIndex ;
    //获取当前系统时间
    private Date currentTime;

    public WaypointMissionOperatorView(Context context) {
        super(context);
    }

    //region Mission Action Demo
    @RequiresApi(api = Build.VERSION_CODES.N)
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
                mWaypointMission = waypointMissionOperatorTool.createWaypointMission(mList);
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
//                preIndex = getPreIndex();
                currentTime = new Date(System.currentTimeMillis());

                oldMediaFiles = getOldList();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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

                break;

            case R.id.btn_AutoMission:
                loadWayPointMission(waypointMissionOperator);
                uploadWayPointMission(waypointMissionOperator);
                try {
                    startWayPointMission(waypointMissionOperator,SettingsDefinitions.CameraMode.RECORD_VIDEO);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private List<MediaFile> getOldList() {
        List<MediaFile> oldMediaFile = mediaManager.getSDCardFileListSnapshot();
        List<MediaFile> oldMediaFiles = new ArrayList<>();
        oldMediaFiles.addAll(oldMediaFile);

        for(MediaFile mediaFile:oldMediaFiles){
            Log.i("oldMF:",mediaFile.getFileName());
        }
        return oldMediaFiles;
    }

    public void autoDownload(final int preIndex, final Date currentTime) {
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            if (ModuleVerificationUtil.isMediaManagerAvailable()) {
                if (mediaManager == null) {
                    mediaManager = DJISampleApplication.getProductInstance().getCamera().getMediaManager();
                }
                DJISampleApplication.getProductInstance()
                        .getCamera()
                        .stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    ToastUtils.setResultToToast("成功停止录制！");
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                    if (taskScheduler == null) {
//                                        taskScheduler = mediaManager.getScheduler();
//                                        if (taskScheduler != null && taskScheduler.getState() == FetchMediaTaskScheduler.FetchMediaTaskSchedulerState.SUSPENDED) {
//                                            taskScheduler.resume(new CommonCallbacks.CompletionCallback() {
//                                                @Override
//                                                public void onResult(DJIError djiError) {
//
//                                                    if (djiError != null) {
//                                                        ToastUtils.setResultToToast("taskScheduler resume failed: " + djiError.getDescription());
//                                                    }
//
//                                                }
//                                            });
//                                        }
//                                    }
                                    try {
                                        Thread.sleep(20000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    DJISampleApplication.getProductInstance()
                                            .getCamera()
                                            .setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD,
                                                    new CommonCallbacks.CompletionCallback() {
                                                        @Override
                                                        public void onResult(DJIError djiError) {
                                                            try {
                                                                Thread.sleep(1000);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            if (null == djiError) {
                                                                ToastUtils.setResultToToast("成功设置下载模式");
                                                                mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                                                                    @Override
                                                                    public void onResult(DJIError djiError) {
                                                                        try {
                                                                            Thread.sleep(1000);
                                                                        } catch (InterruptedException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        if (djiError == null) {
                                                                            if(ModuleVerificationUtil.isCameraModuleAvailable() && mediaManager != null) {
                                                                                ToastUtils.setResultToToast("开始自动下载");
                                                                                //自动开始下载任务
                                                                                latestMediaFiles = mediaManager.getSDCardFileListSnapshot();
                                                                                if (!oldMediaFiles.isEmpty()){
                                                                                    latestMediaFiles.removeAll(oldMediaFiles);
                                                                                }
                                                                                Log.i("old==latest?", String.valueOf(latestMediaFiles.equals(oldMediaFiles)));
                                                                                for (MediaFile mediaFile : oldMediaFiles) {
                                                                                    Log.i("old", mediaFile.getFileName());
                                                                                }
                                                                                for (MediaFile mediaFile : latestMediaFiles) {
                                                                                    Log.i("latest", mediaFile.getFileName());
                                                                                }
                                                                                //第一种方式
                                                                                downloadByCompareToFileList(latestMediaFiles);
                                                                                //第二种方式
//                                                                                downloadByCompareToFileIndex(preIndex);
                                                                                //第三种方式
//                                                                                downloadByCompareToTimeStamp(currentTime);
                                                                            }
                                                                        }else {
                                                                            ToastUtils.setResultToToast("刷新:"+djiError.getDescription());
                                                                        }
                                                                    }
                                                                });
                                                            }else {
                                                                ToastUtils.setResultToToast("模式不支持"+djiError.getDescription());
                                                            }
                                                        }
                                                    });
                                }else{
                                    ToastUtils.setResultToToast("停止录制失败！"+djiError.getDescription());
                                    Log.i("StopRecord:",djiError.getDescription());
                                }
                            }
                        });
            } else {
            }
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
            // CompleteFuture cf=new CompleteFuture();
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
                                // cf.complete（）；
                            }else{
                                ToastUtils.setResultToToast("停止录制报错！"+djiError);
                                // cf.completeException();
                            }
                        }
                    });
//          String ret = cf.get(5,TimeUnit.Second);

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
    public void downloadByCompareToFileList(List<MediaFile> mediaFiles) {
        Log.i("filenameone","执行了！"+mediaFiles.size());
        final File destDir = new File(Environment.getExternalStorageDirectory().
                getPath() + "/Future_List_file/");
        for (final MediaFile mediaFile : mediaFiles) {
            Log.i("filenameone",mediaFile.getFileName());
            mediaFile.fetchFileData(destDir, ((mediaFile.getFileName())
                    .replace(".jpg",""))
                    .replace(".mov",""), new DownloadHandler<String>() {
            });
        }
    }

    //下载最新代码通过获取时间戳
    public void downloadByCompareToTimeStamp(Date date){
        if(ModuleVerificationUtil.isCameraModuleAvailable() && mediaManager != null){
            final File destDir = new File(Environment.getExternalStorageDirectory()
                    .getPath()+"/DJI_Time_file/");
            List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
            for(final MediaFile mediaFile : mediaFiles){
                Date fileDate = new Date();
                fileDate.setTime(mediaFile.getTimeCreated());
                if(date.compareTo(fileDate)<0){
                    Log.i("filenametwo",mediaFile.getFileName());
                    mediaFile.fetchFileData(destDir,((mediaFile.getFileName())
                            .replace(".jpg",""))
                            .replace(".mov",""),new DownloadHandler<String>());
                }
            }
        }
    }

    //下载文件通过对比File的index
    public void downloadByCompareToFileIndex(int preIndex){
        Log.i("filenamethree",String.valueOf(preIndex));
        final File destDir = new File(Environment.getExternalStorageDirectory()
                .getPath()+"/Index_file/");
        List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
        for(final MediaFile mediaFile : mediaFiles){
            if(mediaFile.getIndex() > preIndex){
                Log.i("filenamethree",mediaFile.getFileName()+mediaFile.getIndex());
                mediaFile.fetchFileData(destDir,((mediaFile.getFileName())
                        .replace(".jpg",""))
                        .replace(".mov",""),new DownloadHandler<String>());
            }
        }

    }

    //获取起飞前的最大的FileIndex
    private int getPreIndex()  {
        if (ModuleVerificationUtil.isMediaManagerAvailable()) {
            if (mediaManager == null) {
                mediaManager = DJISampleApplication.getProductInstance().getCamera().getMediaManager();

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
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        if (null == djiError) {



                                        }else {
                                            ToastUtils.setResultToToast("模式不支持"+djiError.getDescription());
                                        }
                                    }
                                });


                List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
                if(mediaFiles.isEmpty()){
                    Log.i("PPPPPPPPPPP:","media是空的");
                    return 0;
                }
                for(MediaFile mediaFile:mediaFiles){
                    preIndex = Math.max(preIndex,mediaFile.getIndex());
                    Log.i("PPPPPPPPPPP:","ansdn.kasndansd");
                }
                Log.i("preIndex:", "asbdakj,dabkj.");
            }
        }
        return preIndex;
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
        if (ModuleVerificationUtil.isMediaManagerAvailable()) {
            if (mediaManager == null) {
                mediaManager = DJISampleApplication.getProductInstance().getCamera().getMediaManager();

            }
        }

//       preIndex = getPreIndex();

        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            if (ModuleVerificationUtil.isMediaManagerAvailable()) {
                if (mediaManager == null) {
                    mediaManager = DJISampleApplication.getProductInstance().getCamera().getMediaManager();
                }

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
                                            fetchMediaList();
                                        }
                                    }
                                });
            } else {
               ToastUtils.setResultToToast(String.valueOf(R.string.not_support_mediadownload));
            }
        }
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

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onExecutionFinish(@Nullable DJIError djiError) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downloadMissionFileByCompletableFuture();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                //之前的下载方式
//                ToastUtils.setResultToToast("Execution finished!"+preIndex);
//                updateWaypointMissionState();
//                flightController.startGoHome(new CommonCallbacks.CompletionCallback() {
//                    @Override
//                    public void onResult(DJIError djiError) {
//                        if(djiError == null){
//                            ToastUtils.setResultToToast("成功返航！");
//                            try {
//                                Thread.sleep(1000);
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        autoDownload(preIndex,currentTime);
//                                    }
//                                }).start();
//
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }else{
//                            ToastUtils.setResultToToast("未成功返航："+djiError.getDescription());
//                        }
//                    }
//                });

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

    private void fetchMediaList() {
        if (ModuleVerificationUtil.isMediaManagerAvailable()) {
            if (mediaManager != null) {
                mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        String str;
                        if (null == djiError) {
                            List<MediaFile> djiMedias = mediaManager.getSDCardFileListSnapshot();

                            if (null != djiMedias) {
                               preIndex = djiMedias.get(djiMedias.size()-1).getIndex();

                                } else {
                                    str = "No Media in SD Card";

                                }

                        } else {
                           ToastUtils.setResultToToast(djiError.getDescription());
                        }
                    }
                });
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void downloadMissionFileByCompletableFuture() throws InterruptedException, ExecutionException, TimeoutException {

                final CompletableFuture<Boolean> goHomeFuture = new CompletableFuture();
                flightController.startGoHome(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            goHomeFuture.complete(true);
                        } else {
                            Log.i(FUTURETESTTAG, "1.error.未能成功返航"+djiError.getDescription());
                            goHomeFuture.completeExceptionally(new Exception(djiError.getDescription()));
                        }
                    }
                });
                boolean goHomeState = goHomeFuture.get();

                if (goHomeState == true) {
                    Log.i(FUTURETESTTAG, "1.成功返航");
                }

              //  Thread.sleep(10000);

                final CompletableFuture<Boolean> stopRecordVideoFuture = new CompletableFuture();
                DJISampleApplication.getProductInstance().getCamera().stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            stopRecordVideoFuture.complete(true);
                        } else {
                            Log.i(FUTURETESTTAG, "2.error.未能成功停止录制"+djiError.getDescription());
                            stopRecordVideoFuture.completeExceptionally(new Exception(djiError.getDescription()));
                        }
                    }
                });
                boolean stopRecordSate = stopRecordVideoFuture.get();
                if (stopRecordSate == true) {
                    Log.i(FUTURETESTTAG, "2.成功停止录制");
                }

                Thread.sleep(10000);

                final CompletableFuture<Boolean> setDownloadModeFuture = new CompletableFuture();
                DJISampleApplication.getProductInstance().getCamera().setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            setDownloadModeFuture.complete(true);
                        } else {
                            Log.i(FUTURETESTTAG, "3.error.未能将相机设置为下载模式"+djiError.getDescription());
                            setDownloadModeFuture.completeExceptionally(new Exception("djiError:" + djiError.getDescription()));
                        }
                    }
                });
                boolean setModeSate = setDownloadModeFuture.get();
                if (setModeSate == true) {
                    Log.i(FUTURETESTTAG, "3.将相机设置为下载模式");
                }
                final CompletableFuture<Boolean> refreshFileListFuture = new CompletableFuture();
                mediaManager.refreshFileList(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            refreshFileListFuture.complete(true);
                        } else {
                            Log.i(FUTURETESTTAG, "4.error.未能执行刷新文件列表"+djiError.getDescription());
                            refreshFileListFuture.completeExceptionally(new Exception(djiError.getDescription()));
                        }
                    }
                });


                Boolean refreshSate = refreshFileListFuture.get();
                if (refreshSate == true) {
                    Log.i(FUTURETESTTAG, "4.MediaFile refresh 成功");
                }
                List<MediaFile> newMediaFiles = mediaManager.getSDCardFileListSnapshot();
                newMediaFiles = newMediaFiles.subList(oldMediaFiles.size(),newMediaFiles.size());

                Log.i(FUTURETESTTAG, "5.开始下载");
                downloadByCompareToFileList(newMediaFiles);

    }

    /**
     * 加载路点任务
     * @param waypointMissionOperator 路点任务操作者
     * @return DJIError 路点任务加载的结果
     */
    public DJIError loadWayPointMission(WaypointMissionOperator waypointMissionOperator){
        WaypointMissionOperatorTool waypointMissionOperatorTool = new WaypointMissionOperatorTool();
        //1.将路点任务文件转换为路点任务链表集合
//        List<Waypoint> waypointsList = waypointMissionOperatorTool.getDjiWayPointFromMyWayPoints(getResources().getString(R.string.my_way_points));
        List<Waypoint> waypointsList = waypointMissionOperatorTool.getDjiWayPointFromMyWayPoints("asdjbajsd");
        //2.创建路点任务文件
        WaypointMission waypointMission = waypointMissionOperatorTool.createWaypointMission(waypointsList);
        //3.路点任务操作者加载路点任务
        DJIError loadMissionError = waypointMissionOperator.loadMission(waypointMission);
        //将路点任务加载的结果返回
        return loadMissionError;
    }

    /**
     * 上传路点任务
     * @param waypointMissionOperator 路点任务操作者
     */
    public void uploadWayPointMission(WaypointMissionOperator waypointMissionOperator){
        if (WaypointMissionState.READY_TO_RETRY_UPLOAD.equals(waypointMissionOperator.getCurrentState())
                || WaypointMissionState.READY_TO_UPLOAD.equals(waypointMissionOperator.getCurrentState())) {
            waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    String djiErrorString  = djiError == null ? "Action started!" : djiError.getDescription();
                    Log.e("WayPointUpLoad",djiErrorString);
                }
            });
        }else {
            ToastUtils.setResultToToast("请先准备好路点任务！");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startWayPointMission(WaypointMissionOperator waypointMissionOperator,SettingsDefinitions.CameraMode cameraMode) throws ExecutionException, InterruptedException {
        //1.获取飞机SD的在开始任务之前的状态
        preIndex = getPreIndex();//最大的index
        oldMediaFiles = getOldList();//之前的MediaFileList
        currentTime = new Date(System.currentTimeMillis());//当前系统之间时间

        //2.开始任务
        final CompletableFuture<Boolean> startMissionFuture = new CompletableFuture<>();
        waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                String startWayPointMission  =  djiError == null? "成功开始任务": djiError.getDescription();
                Log.i("startWayPointMission",djiError.getDescription());
                if(djiError == null){
                    startMissionFuture.complete(true);
                }else{
                    startMissionFuture.completeExceptionally(new Exception(djiError.toString()));
                }
            }
        });
        startMissionFuture.get();
        //3.将相机设置为录制模式
        final CompletableFuture<Boolean> setCameraModeFuture = new CompletableFuture();
        DJISampleApplication.getProductInstance().getCamera().setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                String setCameraMode = djiError == null ? "成功设置相机模式": djiError.getDescription();
                Log.i("setCameraMode",setCameraMode);
                if (djiError == null) {
                    setCameraModeFuture.complete(true);
                } else {
                    setCameraModeFuture.completeExceptionally(new Exception(djiError.getDescription()));
                }
            }
        });
        setCameraModeFuture.get();
        //4.开始录制
        final CompletableFuture<Boolean> startRecordFuture = new CompletableFuture<>();
        if(cameraMode.equals(SettingsDefinitions.CameraMode.RECORD_VIDEO)){
            DJISampleApplication.getProductInstance().getCamera().startRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    String startRecord = djiError == null ? "成功开始录制": djiError.getDescription();
                    Log.i("startRecord",startRecord);
                    if(djiError == null){
                        startRecordFuture.complete(true);
                    }else{
                        startRecordFuture.completeExceptionally(new Exception(djiError.toString()));
                    }
                }
            });
        }else{
            startRecordFuture.isDone();
        }
    }
}
