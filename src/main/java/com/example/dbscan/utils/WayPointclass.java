package com.example.dbscan.utils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
public class WayPointclass {
    String _class;
    List<Point> points = new ArrayList<>();
    List<Integer> cores = new ArrayList<Integer>();
    HashMap<Integer, Integer> clusterMapping = new HashMap<>();
    int clusterGlobalID = 0;

    public WayPointclass(String s) {
        this._class = s;
    }

    // 添加点的方法
    public void addPoint(Point point) {
        points.add(point);
    }

    // 打印所有点的信息
    // 将所有点的信息保存到CSV文件中
    public void savePointsInfoToCSV(String filePath) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // 写入CSV文件头
            bw.write("Timestamp,Latitude,Longitude,SOG,COG,Type,Visited,Classed,ClusterIndex\n");

            // 遍历points列表，将每个点的信息写入文件
            for (Point point : points) {
                bw.write(sdf.format(point.timestamp) + "," +
                        point.latitude + "," +
                        point.longitude + "," +
                        point.sog + "," +
                        point.cog + "," +
                        point.type + "," +
                        point.visited + "," +
                        point.classed + "," +
                        point.clusterIndex + "\n");
            }

        } catch (IOException e) {
            System.err.println("Error writing the CSV file: " + e.getMessage());
        }
    }
}
