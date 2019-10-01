package com.dji.sdk.sample.internal.controller;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.demo.missionoperator.WaypointMissionOperatorTool;
import com.dji.sdk.sample.internal.model.FileInput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.util.CommonCallbacks;
import dji.sdk.mission.waypoint.WaypointMissionOperator;

public class MyWayPointMissionActivity extends AppCompatActivity implements View.OnClickListener{

    private Button startMission;
    private Button stopMission;
    private WaypointMissionOperator mWaypointMissionOperator;
    private WaypointMission mWaypointMission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_way_point_mission_demo);

        startMission = findViewById(R.id.startMission);
        stopMission = findViewById(R.id.stopMission);

        startMission.setOnClickListener(this);
        stopMission.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
                //开始巡航
            case R.id.startMission:
                Toast.makeText(this,"执行了开始巡航！",Toast.LENGTH_LONG).show();
                WaypointMissionOperatorTool waypointMissionOperatorTool = new WaypointMissionOperatorTool();
//                FileInput file = new FileInput("D:\\ProgramFiles(x86)\\Android\\Mobile-SDK-Android\\Sample Code\\app\\src\\main\\res\\测试路线.txt");
                saveByAndroid("test.txt","{\n" +
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
                        "            \"gimbalPitch\": -71.4000015258789,\n" +
                        "            \"lat\": 22.589784056531727,\n" +
                        "            \"lon\": 113.98036856346226,\n" +
                        "            \"shootPhoto\": 1,\n" +
                        "            \"yaw\": 134.20000004768372\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"absoluteAltitude\": 76.23529815673828,\n" +
                        "            \"gimbalPitch\": -75.80000305175781,\n" +
                        "            \"lat\": 22.589431030194767,\n" +
                        "            \"lon\": 113.98069291229368,\n" +
                        "            \"shootPhoto\": 1,\n" +
                        "            \"yaw\": 155.70000004768372\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"absoluteAltitude\": 76.06767272949219,\n" +
                        "            \"gimbalPitch\": -76.9000015258789,\n" +
                        "            \"lat\": 22.58931317982019,\n" +
                        "            \"lon\": 113.98039819095852,\n" +
                        "            \"shootPhoto\": 1,\n" +
                        "            \"yaw\": 154.59999990463257\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"endPosition\": {\n" +
                        "        \"absoluteAltitude\": 76.25504302978516,\n" +
                        "        \"gimbalPitch\": -76.9000015258789,\n" +
                        "        \"lat\": 22.58946024179878,\n" +
                        "        \"lon\": 113.98026310755915,\n" +
                        "        \"yaw\": 126\n" +
                        "    }\n" +
                        "  \n" +
                        "}");
                List<Waypoint> mList = null;

                mList =  waypointMissionOperatorTool.getDjiWayPointFromMyWayPoints(loadByAndroid("test.txt"));
                mWaypointMission = waypointMissionOperatorTool.createWaypointMission(mList);
                //上传任务
                if (WaypointMissionState.READY_TO_RETRY_UPLOAD.equals(mWaypointMissionOperator.getCurrentState())
                        || WaypointMissionState.READY_TO_UPLOAD.equals(mWaypointMissionOperator.getCurrentState())) {
                    mWaypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            String djiErrorString  = djiError == null ? "Action started!" : djiError.getDescription();
                            Log.e("未能成功上传：",djiErrorString);
                        }
                    });
                }else {
                    Toast.makeText(this,"请先准备好路点任务！",Toast.LENGTH_LONG).show();
                }
                //开始任务
                if (mWaypointMission != null) {
                    mWaypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            String djiErrorString  = djiError == null ? "Action started!" : djiError.getDescription();
                            Log.e("未能开始任务：",djiErrorString);
                        }
                    });
                } else {
                    Toast.makeText(this,"请先准备好路点任务！",Toast.LENGTH_LONG).show();
                }
                break;
                //结束巡航
            case R.id.stopMission:
                Toast.makeText(this,"执行了停止巡航！",Toast.LENGTH_LONG).show();
                Toast.makeText(this,"执行了停止巡航！",Toast.LENGTH_LONG).show();
                mWaypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        String djiErrorString  = djiError == null ? "Action started!" : djiError.getDescription();
                        Log.e("未能开始任务：",djiErrorString);
                    }
                });
                break;
                default:
        }
    }

    //保存到文件
    public void saveByAndroid(String fileName,String data){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try{
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
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
    //从文件中加载
    public String loadByAndroid(String fileName){
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder contentOfFile = new StringBuilder();
        try{
            in = openFileInput(fileName);
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
}
