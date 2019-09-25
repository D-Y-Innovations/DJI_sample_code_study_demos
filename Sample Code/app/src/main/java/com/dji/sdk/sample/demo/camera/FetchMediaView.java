package com.dji.sdk.sample.demo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.DownloadHandler;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.BaseThreeBtnView;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import org.bouncycastle.asn1.cms.MetaData;

import dji.common.error.DJICameraError;
import dji.logic.album.manager.litchis.DJIFileResolution;
import dji.logic.album.manager.litchis.DJIFileType;
import dji.logic.album.model.DJIAlbumFileInfo;
import dji.sdk.media.FetchMediaTask;
import dji.sdk.media.FetchMediaTaskContent;
import dji.sdk.media.FetchMediaTaskScheduler;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import java.io.File;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.thirdparty.sanselan.common.ImageMetadata;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Class for fetching the media.
 */
public class FetchMediaView extends BaseThreeBtnView {
    private final String fileProperty = "FileProperty";
    private final String strDateFormat = "yyyy-MM-dd HH:mm:ss";
    private MediaFile media;
    private MediaFile mediaFile;
    private MediaManager mediaManager;
    private FetchMediaTaskScheduler taskScheduler;
    private FetchMediaTask.Callback fetchMediaFileTaskCallback;
    private  final String fileSdPath = Environment.getExternalStorageDirectory().getPath();

    public FetchMediaView(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpListener();
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
                changeDescription(R.string.not_support_mediadownload);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, null);
        }
        if (taskScheduler != null) {
            taskScheduler.suspend(null);
        }
    }

    @Override
    protected int getMiddleBtnTextResourceId() {
        return R.string.fetch_media_view_fetch_thumbnail;
    }

    @Override
    protected int getLeftBtnTextResourceId() {
        return R.string.fetch_media_view_fetch_preview;
    }

    @Override
    protected int getRightBtnTextResourceId() {
        return R.string.fetch_media_view_fetch_media;
    }

    @Override
    protected int getDescriptionResourceId() {
        if (!ModuleVerificationUtil.isMediaManagerAvailable()) {
            return R.string.not_support_mediadownload;
        } else {
            return R.string.support_mediadownload;
        }
    }

    @Override
    protected void handleMiddleBtnClick() {
        // Fetch Thumbnail Button
        if (ModuleVerificationUtil.isMediaManagerAvailable()
                && media != null
                && mediaManager != null
                && taskScheduler != null) {

            taskScheduler.moveTaskToEnd(new FetchMediaTask(media,
                    FetchMediaTaskContent.THUMBNAIL,
                    fetchMediaFileTaskCallback));


        }
    }

    @Override
    protected void handleLeftBtnClick() {
        // Fetch Preview Button
        if (ModuleVerificationUtil.isMediaManagerAvailable()
                && media != null
                && mediaManager != null
                && taskScheduler != null) {
            taskScheduler.moveTaskToEnd(new FetchMediaTask(media,
                    FetchMediaTaskContent.PREVIEW,
                    fetchMediaFileTaskCallback));
        }
    }

    //下载
    @Override
    protected void handleRightBtnClick() {
        // Fetch Media Data Button
        if (ModuleVerificationUtil.isCameraModuleAvailable()
                && media != null
                && mediaManager != null) {

            List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
            //下载SD上的全部文件
            downloadLatestFiles(mediaFiles);
//            classificationDownload(mediaFiles);
            //重置SD卡
//            resetSD(mediaFiles);
            //从文件头中获取Image desc信息(第一种方式)
//            getFileExif("drawable-hdpi/a0007.jpg");
            //从文件头中获取Image desc信息(第二种方式)
//            getFileMedata();
//            getMedataLog();

            media.fetchPreview(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
        }
    }

    private void classificationDownload(List<MediaFile> mediaFiles) {
        ToastUtils.setResultToToast("开始classificationDownload！");
        //文件尺寸阈值
        final int fileSizeThreshold = 1000000;
        final int visibleVideoResolutionWidth = 3840;
        final int visibleVideoResolutionHeight = 2160;
        final int thresholdVideoResolutionWidth = 640;
        final int thresholdVideoResolutionHeight = 512;
        final String fileSdPath = Environment.getExternalStorageDirectory().getPath();
        final File visibleImage = new File(Environment.getExternalStorageDirectory().getPath()+"/DJI_Sdk_XT2_Visible_Images/");
        final File thermalImage = new File(Environment.getExternalStorageDirectory().getPath()+"/DJI_Sdk_XT2_thermal_Images/");
        final File visibleVideo = new File(Environment.getExternalStorageDirectory().getPath()+"/DJI_Sdk_XT2_Visible_videos/");
        final File thermalVideo = new File(Environment.getExternalStorageDirectory().getPath()+"/DJI_Sdk_XT2_thermal_videos/");

        for(MediaFile mediaFile: mediaFiles){
            DJIAlbumFileInfo djiAlbumFileInfo = mediaFile.getInnerFileInfo();
            DJIFileResolution resolution = djiAlbumFileInfo.resolution;
            String fileName = ((mediaFile.getFileName()).replace(".jpg","")).replace(".mov","");
            if(djiAlbumFileInfo.fileType.toString().equals("JPG")){
                if (mediaFile.getIndex() % 2 == 1 && mediaFile.getFileSize() < fileSizeThreshold) {
                    mediaFile.fetchFileData(thermalImage, fileName, new DownloadHandler<String>());
                } else if(mediaFile.getIndex() % 2 == 0 && mediaFile.getFileSize() > fileSizeThreshold) {
                    mediaFile.fetchFileData(visibleImage, fileName, new DownloadHandler<String>());
                }else {
                    ToastUtils.setResultToToast("非可见光和热成像图片！");
                }
            }else if(djiAlbumFileInfo.fileType.toString().equals("MOV")){
                if (resolution.c()== visibleVideoResolutionWidth && resolution.d()==visibleVideoResolutionHeight ) {
                    mediaFile.fetchFileData(visibleVideo, fileName, new DownloadHandler<String>());
                } else if(resolution.c()== thresholdVideoResolutionHeight && resolution.d() == thresholdVideoResolutionWidth ){
                    mediaFile.fetchFileData(thermalVideo, fileName, new DownloadHandler<String>());
                }else {
                    ToastUtils.setResultToToast("非可见光和热成像视频！");
                }
            }else {
                ToastUtils.setResultToToast("此文件非视频和图片，放弃下载！");
            }
        }
    }

    //获取图片的元数据通过Medata方法
    public String getFileMedata(){

        Log.i("getMedata:","执行了getFileMedata！");
        File destDir = new File(Environment.getExternalStorageDirectory().
                getPath() + "/Dji_Sdk_XT2Files/DJI_0007.jpg");
        String desc = new String();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(destDir);
            for(Directory directory:metadata.getDirectories()) {
                if (directory == null) {
                    continue;
                }
                for (Tag tag : directory.getTags()) {
//                    if ((tag.getTagName()).equals(": Image Description") && tag.getDescription().endsWith("_R.JPG")) {
                    if (tag.getDescription().endsWith("_R.JPG")) {
                        Log.i("Lijun:", tag.getDescription());
                    }
                }

            }

        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return desc;
    }

    private void getMedataLog() {
        Log.i("getMedata:","执行了getFileMedata！");
        File destDir = new File(Environment.getExternalStorageDirectory().
                getPath() + "/Dji_Sdk_XT2Files/DJI_0007.jpg");
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(destDir);
            int i = 0;

            for(Directory directory:metadata.getDirectories()){
                if(directory == null){
                    continue;
                }
                String a = directory.getDescription(i++);
                Log.i("JJ","外层：第"+i+"层");
                Log.i("JJDirectoryName",directory.getName());
                int j = 0;
                for(Tag tag:directory.getTags()){
                    Log.i("JJJJTagName:",tag.getTagName());
                    Log.i("JJJJDirectoryName:",tag.getDirectoryName());
                    Log.i("JJJJTagType:", String.valueOf(tag.getTagType()));
                    Log.i("JJJJTagTypeHex:",tag.getTagTypeHex());
                    Log.i("JJJJDescription:",tag.getDescription());
                    Log.i("JJJJTagName", String.valueOf(tag.hasTagName()));
                    Log.i("JJJJtoString:",tag.toString());
                    j++;
                    Log.i("JJJJ:","第"+j+"个");
                }
            }
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取图片的元数据通过ExifInterface方法
    public String getFileExif(String fileName)  {
        Log.i("getFile:","执行了getFileExif！");

        File destDir = new File(Environment.getExternalStorageDirectory().
                getPath() + "/Dji_Sdk_XT2Files/DJI_0007.jpg");
        String filePath = fileSdPath + "/Dji_Sdk_XT2Files/" +"DJI_0007.jpg";
        File destDir2 = new File(Environment.getExternalStorageDirectory().
                getPath() + "/Dji_Sdk_XT2Files/");
        Log.i("FileExists:", String.valueOf(destDir2.exists()));

        if(destDir.exists()){
            Log.i("FileExists:","图片文件存在！");
        }else{
            Log.i("Unexists:","文件不存在！");
        }
        String imageDesc = "没有图片信息";

        try {
            ExifInterface exifIn = new ExifInterface(String.valueOf(destDir));
            imageDesc = exifIn.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
            Log.i("ImageDescTry",imageDesc+exifIn.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("ImageDescCatch",imageDesc);
        }

        return imageDesc;
    }

    //下载最新的图片视频文件
    public void downloadLatestFiles(List<MediaFile> mediaFiles) {
        if (ModuleVerificationUtil.isCameraModuleAvailable()
                && mediaManager != null) {
            final File destDir = new File(Environment.getExternalStorageDirectory().
                    getPath() + "/Dji_Sdk_XT3Files/");

            Log.i("StartDownload", String.valueOf(destDir.exists()));
            for(int i = 0; i<=mediaFiles.size()-1;i++){
                mediaFile = mediaFiles.get(i);
//                getMediaFileProperty(mediaFile);
                Log.i("StartDownload","开始下载");
                mediaFile.fetchFileData(destDir,
                        ((mediaFile.getFileName())
                                .replace(".jpg",""))
                                .replace(".mov",""),
                        new DownloadHandler<String>() {});
                getMediaFileProperty(mediaFile);



            }
//            for (final MediaFile mediaFile : mediaFiles) {
//                mediaFile.fetchFileData(destDir, mediaFile.getFileName(), new DownloadHandler<String>() {
//                });
//            }

        }
    }
    //获取当前下载文件的一些属性值属性
    private void getMediaFileProperty(MediaFile mediaFile) {
        Date date = new Date();
        date.setTime(mediaFile.getTimeCreated());
        String timeCreated  = new SimpleDateFormat(strDateFormat).format(date);
        DJIAlbumFileInfo djiAlbumFileInfo = mediaFile.getInnerFileInfo();
        DJIFileResolution resolution = djiAlbumFileInfo.resolution;
        DJIFileType djiFileType = djiAlbumFileInfo.fileType;

        Log.i(fileProperty,"FileIndex:"+mediaFile.getIndex()+","
                +"SubIndex:"+mediaFile.getSubIndex()+","
                +"FileName:"+mediaFile.getFileName()+","
                +"FileSize:"+mediaFile.getFileSize()+","
                +"videoResolution:"+mediaFile.getResolution()+","
                +"videoResolutionValue:"+mediaFile.getResolution().value()+","
                +"videoResolutionRatio:"+mediaFile.getResolution().ratio()+","
                +"videoResolutionCmdValue:"+mediaFile.getResolution().cmdValue()+","
                +"TimeCreated:"+mediaFile.getDateCreated()+","
                +"djiFileType:"+djiFileType.toString()+","
        +"resolution:"+resolution.a()+"_"+resolution.b()+"_"+resolution.c()+"_"+resolution.d());

    }

    //重置SD卡
    public void resetSD(List<MediaFile> mediaFiles) {
        mediaManager.deleteFiles(mediaFiles, new CommonCallbacks.CompletionCallbackWithTwoParam<List<MediaFile>, DJICameraError>() {
            @Override
            public void onSuccess(List<MediaFile> mediaFiles, DJICameraError djiCameraError) {
                ToastUtils.setResultToToast("重置成功！"+djiCameraError);
            }

            @Override
            public void onFailure(DJIError djiError) {
                ToastUtils.setResultToToast("重置失败！"+djiError);
            }
        });
    }

    private void setUpListener() {
        // Example of Listener
        fetchMediaFileTaskCallback = new FetchMediaTask.Callback() {
            @Override
            public void onUpdate(MediaFile mediaFile, FetchMediaTaskContent fetchMediaTaskContent, DJIError djiError) {

                if (djiError == null) {
                    Bitmap bitmap = null;
                    if (FetchMediaTaskContent.PREVIEW == fetchMediaTaskContent) {
                        bitmap = mediaFile.getPreview();
                    }
                    if (FetchMediaTaskContent.THUMBNAIL == fetchMediaTaskContent) {
                        bitmap = mediaFile.getThumbnail();
                    }
                } else {
                    ToastUtils.setResultToToast("fetch media failed: " + djiError.getDescription());
                }
            }
        };
    }

    // Initialize the view with getting a media file.
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
                                if (!djiMedias.isEmpty()) {
                                    media = djiMedias.get(0);
                                    str = "Total Media files:" + djiMedias.size() + "\n" + "Media 1: " +
                                            djiMedias.get(0).getFileName();
                                    changeDescription(str);
                                } else {
                                    str = "No Media in SD Card";
                                    changeDescription(str);
                                }
                            }
                        } else {
                            changeDescription(djiError.getDescription());
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getDescription() {
        return R.string.camera_listview_download_media;
    }
}
