package com.example.dbscan.pojo;

import java.util.Date;

public class LabelPoint {
    private double latitude;
    private double longitude;
    private int cluster;
    private int category;
    private long MMSI;
    private String time;

    // 构造函数
    public LabelPoint(double latitude, double longitude, int cluster, int category, long MMSI, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.cluster = cluster;
        this.category = category;
        this.MMSI = MMSI;
        this.time = time;

    }

    // getter和setter方法
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public long getMMSI() {
        return MMSI;
    }

    public void setMMSI(long MMSI) {
        this.MMSI = MMSI;
    }

    public String getTime(){return  time;}

    public void setTime(String time){this.time=time;}

}

