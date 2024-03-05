package com.example.dbscan.pojo;

import java.util.Date;

public class SinglePoint {
    private double cog;
    private double sog;
    private Date timestamp;
    private double longitude;
    private double latitude;
    private String MMSI;
    private String name;
    private String type;

    public SinglePoint() {
    }

    public SinglePoint(double cog, double sog, Date timestamp, double longitude, double latitude, String MMSI, String name, String type) {
        this.cog = cog;
        this.sog = sog;
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
        this.MMSI = MMSI;
        this.name = name;
        this.type = type;
    }

    public double getCog() {
        return cog;
    }



    public void setCog(double cog) {
        this.cog = cog;
    }

    public double getSog() {
        return sog;
    }

    public void setSog(double sog) {
        this.sog = sog;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getMMSI() {
        return MMSI;
    }

    public void setMMSI(String MMSI) {
        this.MMSI = MMSI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
//        return "SinglePoint{" +
//                "cog=" + cog +
//                ", sog=" + sog +
//                ", timestamp=" + timestamp +
//                ", longitude=" + longitude +
//                ", latitude=" + latitude +
//                ", MMSI='" + MMSI + '\'' +
//                ", name='" + name + '\'' +
//                ", type='" + type + '\'' +
//                '}';
        return  cog +
                "," + sog +
                "," + timestamp +
                "," + longitude +
                "," + latitude +
                "," + MMSI +
                "," + name +
                "," + type +
                '}';
    }
}
