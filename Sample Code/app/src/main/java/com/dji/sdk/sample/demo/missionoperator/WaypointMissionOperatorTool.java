package com.dji.sdk.sample.demo.missionoperator;


import com.dji.sdk.sample.internal.model.MyWayPoints;

import java.util.ArrayList;
import java.util.List;

import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.waypoint.WaypointMissionOperator;

/**
 * WaypointMissionOperatorTool
 */
public class WaypointMissionOperatorTool {
    private WaypointMissionOperator mWaypointMissionOperator;
    public WaypointMissionOperatorTool(){
        if(mWaypointMissionOperator == null){
            mWaypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
        }
    }

    //将json字符串数据转化为路点数据。
    public List<Waypoint> getDjiWayPointFromMyWayPoints(String myWayPointsInJson){
        List<Waypoint> result=new ArrayList<>();
        MyWayPoints myWayPoints=MyWayPoints.fromJsonString(myWayPointsInJson);
        if(myWayPoints==null){
            return result;
        }
        MyWayPoints.StartPositionBean startPositionBean=myWayPoints.getStartPosition();
        Waypoint startWp=new Waypoint(startPositionBean.getLat()
        ,startPositionBean.getLon(),(float) startPositionBean.getAbsoluteAltitude());
        result.add(startWp);
        for (MyWayPoints.GpsListBean gpsListBean : myWayPoints.getGpsList()) {
            Waypoint waypoint=new Waypoint(gpsListBean.getLat(),gpsListBean.getLon(),
                    (float) gpsListBean.getAbsoluteAltitude());
            if(gpsListBean.getShootPhoto()==1){
                waypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,(int)gpsListBean.getYaw()));
                waypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH,(int)gpsListBean.getGimbalPitch()));
                waypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO,0));
            }
            result.add(waypoint);
        }
        MyWayPoints.EndPositionBean endPositionBean = myWayPoints.getEndPosition();
        Waypoint endWp = new Waypoint(endPositionBean.getLat(),endPositionBean.getLon(),(float)endPositionBean.getAbsoluteAltitude());
        result.add(endWp);
        return  result;
    }

    //创建路点任务
    public WaypointMission ctreateWaypointMission(List<Waypoint> waypointArray){
        WaypointMission.Builder builder = new WaypointMission.Builder();
        builder.autoFlightSpeed(5f);
        builder.maxFlightSpeed(10f);
        builder.setExitMissionOnRCSignalLostEnabled(false);
        builder.finishedAction(WaypointMissionFinishedAction.NO_ACTION);
        builder.flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY);
        builder.headingMode(WaypointMissionHeadingMode.AUTO);
        builder.repeatTimes(1);

        //将坐标集合给任务
        builder.waypointList(waypointArray).waypointCount(waypointArray.size());
        return builder.build();
    }
}
