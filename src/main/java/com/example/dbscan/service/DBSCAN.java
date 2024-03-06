package com.example.dbscan.service;

import com.example.dbscan.pojo.SinglePoint;
import com.example.dbscan.utils.Algorithm1;
import java.util.Collections;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

public class DBSCAN {
    public int calculate=0;
    public List<String[]> PointLog = new ArrayList<>();
    Algorithm1 algorithm1 = new Algorithm1();
    public List<SinglePoint> DBscanlist() {

        List<SinglePoint> list = new ArrayList<>();

        // PostgreSQL数据库连接信息
        String jdbcUrl = "jdbc:postgresql://localhost:5432/NewAISClean";
        String username = "postgres";
        String password = "postgres";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL查询语句
            String sql = "SELECT mmsi, time, lon, lat, course, speed, status FROM  \"new20180430_08_09\"";
//            String sql = "SELECT mmsi, time, mercator_x, mercator_y, course, speed, status FROM  \"new201804_clean\"";
            System.out.println("数据库查询成功");
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                int rowCount = 0;  // 计数器，用于记录查询结果的行数

                while (resultSet.next()) {
                    rowCount++;  // 每次循环增加一行计数
                    String MMSI = resultSet.getString("mmsi");
                    String timestamp = resultSet.getString("time");
                    String longitude = resultSet.getString("lon");
                    String latitude = resultSet.getString("lat");
//                    String longitude = resultSet.getString("mercator_x");
//                    String latitude = resultSet.getString("mercator_y");
                    String cog = resultSet.getString("course");
                    String sog = resultSet.getString("speed");
                    String type = resultSet.getString("status");

                    handleNewData(MMSI, timestamp, latitude, longitude, sog, cog, type);//计算DBSCAN参数并调用URE

//                    algorithm1.URE(MMSI, timestamp, latitude, longitude, sog, cog, type);

//                    System.out.println("longitude:"+longitude+"latitude:"+latitude+"time: "+timestamp);
                }

                System.out.println("查询结果共有 " + rowCount + " 行。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < algorithm1.routes.size(); i++) {
            for (int j = 0; j < algorithm1.routes.get(i).params.size(); j++) {
                double x;
                double y;
                x = algorithm1.routes.get(i).params.get(j).latitude;
                y = algorithm1.routes.get(i).params.get(j).longitude;
                String mmsi = algorithm1.routes.get(i).params.get(j).pointIndex + "";
                Date timestamp = algorithm1.routes.get(i).params.get(j).timestamp;
//                System.out.println("时间测试:"+timestamp);
                double sog = algorithm1.routes.get(i).params.get(j).sog;
                double cog = algorithm1.routes.get(i).params.get(j).cog;
                String type = algorithm1.routes.get(i).params.get(j).type;
                list.add(new SinglePoint(cog, sog, timestamp, y, x, mmsi, algorithm1.routes.get(i).routename, type));
            }
        }
        // 使用HashSet来存储不同的routename
        Set<String> uniqueRouteNames = new HashSet<>();

        // 遍历list收集不同的routename
        for(SinglePoint point : list) {
            uniqueRouteNames.add(point.getName());
        }

        // 输出不同routename的数量
        System.out.println("共有 " + uniqueRouteNames.size() + " 种不同的 routename。");
        return list;
    }

    private void handleNewData(String MMSI, String timestamp, String latitude, String longitude, String sog, String cog, String type) {
        // 将传入的参数保存在数组中并添加到PointLog中
        String[] params = new String[]{MMSI, timestamp, latitude, longitude, sog, cog, type};
        PointLog.add(params);

        // 计算新数据与已有数据的距离
        List<Double> distances = new ArrayList<>();
        for (String[] point : PointLog) {
            double dist = calculateDistance(Double.parseDouble(latitude), Double.parseDouble(longitude), Double.parseDouble(point[2]), Double.parseDouble(point[3]));
            distances.add(dist);
        }

        // 对距离进行排序
        Collections.sort(distances);

        // 计算每两个连续距离之间的增长率
        List<Double> growthRates = new ArrayList<>();
        for (int i = 1; i < distances.size(); i++) {
            double previousDistance = distances.get(i - 1);
            double currentDistance = distances.get(i);
            // 防止除以零
            if (previousDistance != 0) {
                double growthRate = (currentDistance - previousDistance) / previousDistance;
                growthRates.add(growthRate);
            }
        }

        // 找到增长率最大的突变点，即增长率改变最大的位置
        int maxGrowthRateIndex = 0;
        double maxGrowthRateChange = 0;
        for (int i = 1; i < growthRates.size(); i++) {
            double previousGrowthRate = growthRates.get(i - 1);
            double currentGrowthRate = growthRates.get(i);
            double growthRateChange = Math.abs(currentGrowthRate - previousGrowthRate);
            if (growthRateChange > maxGrowthRateChange) {
                maxGrowthRateChange = growthRateChange;
                maxGrowthRateIndex = i;
            }
        }

        // 使用突变点之后的第一个距离作为radius
        double radius = distances.isEmpty() ? 0 : distances.get(Math.min(maxGrowthRateIndex + 1, distances.size() - 1));

//        // 动态调整minpts
//        int minpts = Math.max(3, Math.min(maxGrowthRateIndex + 1, distances.size() / 2));




        // 更新DBSCAN参数
        algorithm1.radius = radius;
        algorithm1.minpts = 4;

        // 调用URE方法
//        System.out.println("调用URE方法，新radius值为: " + radius + "新的minpts是" + algorithm1.minpts);
        algorithm1.URE(MMSI, timestamp, latitude, longitude, sog, cog, type);
        calculate++;
        System.out.println("现在是第"+calculate+"次调用URE");
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径，单位是千米
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // 计算最终距离并返回

        return distance * 1000; // 将千米转换为米
    }


}


