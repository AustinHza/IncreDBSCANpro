package com.example.dbscan.service;

import com.example.dbscan.pojo.SinglePoint;
import com.example.dbscan.utils.Algorithm1;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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

public class DBSCAN {
    public List<SinglePoint> DBscanlist() {
        Algorithm1 algorithm1 = new Algorithm1();
        List<SinglePoint> list = new ArrayList<>();

        // PostgreSQL数据库连接信息
        String jdbcUrl = "jdbc:postgresql://localhost:5432/NewAISClean";
        String username = "postgres";
        String password = "postgres";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL查询语句
            String sql = "SELECT mmsi, time, lon, lat, course, speed, status FROM  \"new20180401_0407_clean\"";
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
                    algorithm1.URE(MMSI, timestamp, latitude, longitude, sog, cog, type);
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
                System.out.println("时间测试:"+timestamp);
                double sog = algorithm1.routes.get(i).params.get(j).sog;
                double cog = algorithm1.routes.get(i).params.get(j).cog;
                String type = algorithm1.routes.get(i).params.get(j).type;
                list.add(new SinglePoint(cog, sog, timestamp, y, x, mmsi, algorithm1.routes.get(i).routename, type));
            }
        }
        return list;
    }
}


