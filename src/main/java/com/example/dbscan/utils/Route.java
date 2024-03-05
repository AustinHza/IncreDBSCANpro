package com.example.dbscan.utils;

import java.util.ArrayList;
import java.util.List;

public class Route {
    String r1;
    String r2;
    public String routename;
    public List<Point> params = new ArrayList<>();

    public Route(String s1, String s2) {
        this.r1 = s1;
        this.r2 = s2;
        this.routename = r1 + r2;
    }
}
