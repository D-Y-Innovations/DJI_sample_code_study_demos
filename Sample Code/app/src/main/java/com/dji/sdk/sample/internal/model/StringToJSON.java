package com.dji.sdk.sample.internal.model;

import com.google.gson.Gson;

import java.util.ArrayList;

public class StringToJSON {
    private String jsonString;
    StringToJSON(String jsonString){
        this.jsonString = jsonString;
    }
    //将传输过来的是数据以指定类型返回
    public ArrayList getStringT(){
        ArrayList arrayList = new ArrayList<>();
        MyWayPoints myWaypoint = new Gson().fromJson(jsonString, MyWayPoints.class);
        return  arrayList;
    }
}
