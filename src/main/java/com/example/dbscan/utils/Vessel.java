package com.example.dbscan.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Vessel {
    List<Point> tracks = new ArrayList<Point>();
    List<String> wps = new ArrayList<>();
    List<Point> timestampwps = new ArrayList<>();
    List<String> routes = new ArrayList<>();
    int MMSI;
    String status;
    double Avgspeed;
    Date lastupdate;

    public Vessel(int mmsi) {
        this.MMSI = mmsi;
    }
}
