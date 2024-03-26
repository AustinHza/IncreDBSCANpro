package com.example.dbscan.pojo;

import java.util.Date;

public class SinglePoint {
    private double cog;
    private double sog;
    private Date timestamp;
    private double mercator_x;
    private double mercator_y;
    private String MMSI;
    private String name;
    private String type;
    private double latitude;
    private double longitude;

    public SinglePoint() {
    }

    public SinglePoint(double cog, double sog, Date timestamp, double mercator_x, double mercator_y, String MMSI, String name, String type, double latitude,double longitude ) {
        this.cog = cog;
        this.sog = sog;
        this.timestamp = timestamp;
        this.mercator_x = mercator_x;
        this.mercator_y = mercator_y;
        this.MMSI = MMSI;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getMercator_x() {
        return mercator_x;
    }

    public void setMercator_x(double mercator_x) {
        this.mercator_x = mercator_x;
    }

    public double getMercator_y() {
        return mercator_y;
    }

    public void setMercator_y(double mercator_y) {
        this.mercator_y = mercator_y;
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
                "," + mercator_x +
                "," + mercator_y +
                "," + MMSI +
                "," + name +
                "," + type +
                "," + latitude +
                "," + longitude +
                '}';
    }
}
