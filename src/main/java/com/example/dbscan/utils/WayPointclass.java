package com.example.dbscan.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WayPointclass {
    String _class;
    List<Point> points = new ArrayList<>();
    List<Integer> cores = new ArrayList<Integer>();
    HashMap<Integer, Integer> clusterMapping = new HashMap<>();
    int clusterGlobalID = 0;

    public WayPointclass(String s) {
        this._class = s;
    }
}
