package com.dji.sdk.sample.internal.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

public class MyWayPoints {

     public static MyWayPoints fromJsonString(String s) {
         Gson gson=new Gson();
         try {
             return gson.fromJson(s, MyWayPoints.class);
         } catch (JsonSyntaxException e) {
             e.printStackTrace();
         }
         return null;
     }

    /**
     * startPosition : {"absoluteAltitude":76.54328155517578,"gimbalPitch":0,"lat":22.589992518798812,"lon":113.98004457818286,"yaw":0}
     * gpsList : [{"absoluteAltitude":76.08706665039062,"gimbalPitch":-71.4000015258789,"lat":22.589784056531727,"lon":113.98036856346226,"shootPhoto":1,"yaw":134.20000004768372},{"absoluteAltitude":76.23529815673828,"gimbalPitch":-75.80000305175781,"lat":22.589431030194767,"lon":113.98069291229368,"shootPhoto":1,"yaw":155.70000004768372},{"absoluteAltitude":76.06767272949219,"gimbalPitch":-76.9000015258789,"lat":22.58931317982019,"lon":113.98039819095852,"shootPhoto":1,"yaw":154.59999990463257}]
     * endPosition : {"absoluteAltitude":76.25504302978516,"gimbalPitch":-76.9000015258789,"lat":22.58946024179878,"lon":113.98026310755915,"yaw":126}
     */

    private StartPositionBean startPosition;
    private EndPositionBean endPosition;
    private List<GpsListBean> gpsList;

    public StartPositionBean getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(StartPositionBean startPosition) {
        this.startPosition = startPosition;
    }

    public EndPositionBean getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(EndPositionBean endPosition) {
        this.endPosition = endPosition;
    }

    public List<GpsListBean> getGpsList() {
        return gpsList;
    }

    public void setGpsList(List<GpsListBean> gpsList) {
        this.gpsList = gpsList;
    }

    public static class StartPositionBean {
        /**
         * absoluteAltitude : 76.54328155517578
         * gimbalPitch : 0
         * lat : 22.589992518798812
         * lon : 113.98004457818286
         * yaw : 0
         */

        private double absoluteAltitude;
        private int gimbalPitch;
        private double lat;
        private double lon;
        private int yaw;

        public double getAbsoluteAltitude() {
            return absoluteAltitude;
        }

        public void setAbsoluteAltitude(double absoluteAltitude) {
            this.absoluteAltitude = absoluteAltitude;
        }

        public int getGimbalPitch() {
            return gimbalPitch;
        }

        public void setGimbalPitch(int gimbalPitch) {
            this.gimbalPitch = gimbalPitch;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public int getYaw() {
            return yaw;
        }

        public void setYaw(int yaw) {
            this.yaw = yaw;
        }
    }

    public static class EndPositionBean {
        /**
         * absoluteAltitude : 76.25504302978516
         * gimbalPitch : -76.9000015258789
         * lat : 22.58946024179878
         * lon : 113.98026310755915
         * yaw : 126
         */

        private double absoluteAltitude;
        private double gimbalPitch;
        private double lat;
        private double lon;
        private int yaw;

        public double getAbsoluteAltitude() {
            return absoluteAltitude;
        }

        public void setAbsoluteAltitude(double absoluteAltitude) {
            this.absoluteAltitude = absoluteAltitude;
        }

        public double getGimbalPitch() {
            return gimbalPitch;
        }

        public void setGimbalPitch(double gimbalPitch) {
            this.gimbalPitch = gimbalPitch;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public int getYaw() {
            return yaw;
        }

        public void setYaw(int yaw) {
            this.yaw = yaw;
        }
    }

    public static class GpsListBean {
        /**
         * absoluteAltitude : 76.08706665039062
         * gimbalPitch : -71.4000015258789
         * lat : 22.589784056531727
         * lon : 113.98036856346226
         * shootPhoto : 1
         * yaw : 134.20000004768372
         */

        private double absoluteAltitude;
        private double gimbalPitch;
        private double lat;
        private double lon;
        private int shootPhoto;
        private double yaw;

        public double getAbsoluteAltitude() {
            return absoluteAltitude;
        }

        public void setAbsoluteAltitude(double absoluteAltitude) {
            this.absoluteAltitude = absoluteAltitude;
        }

        public double getGimbalPitch() {
            return gimbalPitch;
        }

        public void setGimbalPitch(double gimbalPitch) {
            this.gimbalPitch = gimbalPitch;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public int getShootPhoto() {
            return shootPhoto;
        }

        public void setShootPhoto(int shootPhoto) {
            this.shootPhoto = shootPhoto;
        }

        public double getYaw() {
            return yaw;
        }

        public void setYaw(double yaw) {
            this.yaw = yaw;
        }
    }
}
