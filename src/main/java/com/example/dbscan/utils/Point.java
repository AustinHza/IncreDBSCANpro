package com.example.dbscan.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Point {
    public double cog;
    public double sog;
    public Date timestamp;
    public double[] position = new double[2];
    public int pointIndex;
    public double longitude;
    public double latitude;
    public double x;
    public double y;
    //public String partition_id=null;
    public int clusterIndex;
    public int epsNbrNum;
    public boolean visited;
    public boolean classed;
    public final static int NOISE = -1;
    public String type;

    public Point(String mmsi, String timestamp, String latitude, String longitude, String sog, String cog, String type) {
        this.pointIndex = Integer.valueOf(mmsi);
        try {
            this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
        } catch (ParseException e) {
            System.out.println("fuck");
        }
        this.latitude = Double.valueOf(latitude);
        this.longitude = Double.valueOf(longitude);
        this.sog = Double.valueOf(sog.equals("null") ? "10" : sog);
        this.cog = Double.valueOf(cog.equals("null") ? "10" : cog);
        this.type = type;
        //his.partition_id = "P"+Linearr[len-2];
//        double[] xy = coordinate.lonlat2xy(Double.valueOf(latitude), Double.valueOf(longitude));
//        this.x = xy[0];
//        this.y = xy[1];
        this.x = Double.valueOf(longitude);
        this.y = Double.valueOf(latitude);
        this.position[0] = x;
        this.position[1] = y;
        this.visited = false;
        this.classed = false;
        this.clusterIndex = NOISE;  // initially a noise point
        this.epsNbrNum = 1;  // number of eps-neighbors around me
    }

    //这个是欧氏距离计算
    public double DistanceCalculate(Point p1) {
        double sumDistSq = 0.0;
        int d = position.length;
        for (int i = 0; i < d; i++) {
            sumDistSq += (position[i] - p1.position[i]) * (position[i] - p1.position[i]);
        }
        return Math.sqrt(sumDistSq);
    }
//    这个是harversine距离计算，两种距离计算，用哪个就把哪个解开注释
//    public double DistanceCalculate(Point p1) {
//        final int R = 6371000; // 地球平均半径，单位：米
//        double lat1 = Math.toRadians(this.position[0]);
//        double lon1 = Math.toRadians(this.position[1]);
//        double lat2 = Math.toRadians(p1.position[0]);
//        double lon2 = Math.toRadians(p1.position[1]);
//
//        double dLat = lat2 - lat1;
//        double dLon = lon2 - lon1;
//
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(lat1) * Math.cos(lat2) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//
//        return R * c; // 返回结果，单位为米
//    }

	/*public String toString() {
		String info = "Position: ";
		for (int i = 0; i < position.length; i++) {
			info += position[i] + ", ";
		}
		info += "Index: " + pointIndex + ", Label: " + label + ", Visited: " +
				visited + ", ClusterIndex: " + clusterIndex;
		return info;
	}*/


}
