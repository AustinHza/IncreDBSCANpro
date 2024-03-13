package com.example.dbscan.service;

import com.example.dbscan.MyJava.MyJavaClass;
import com.example.dbscan.pojo.LabelPoint;
import com.example.dbscan.pojo.SinglePoint;
import com.example.dbscan.utils.Algorithm1;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Main {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(); // 记录开始时间
//*******************************1.先用增量DBSCAN进行第一次聚类，得到一些主要轨迹************************
        // 创建DBSCAN对象
        DBSCAN dbscan = new DBSCAN();

        // 调用增量DBscanlist函数获取处理后的列表
        List<SinglePoint> resultList = dbscan.DBscanlist();
        //先保存第一步的结果
        String outputFilePath1 = "D:\\1HZA\\YJSBYSJ\\Code\\Algorithm\\IncreDbscantwicejdbcpro\\result\\ec_hv_dbscan1new20180430_08_16min4env0.02.csv";
//        saveToCSV(resultList, outputFilePath);
        saveToCSV1(resultList, outputFilePath1);
        System.out.println("第一步结果保存成功");
        // 打印处理后的列表
/*        for (SinglePoint point : resultList) {
            System.out.println(point);
        }*/
//*******************************2.调用轨迹聚类，对增量DBSCAN聚类的结果进行再次聚类************************
//        MyJavaClass dbscan2 = new MyJavaClass();
//        List<String> finalResultList = dbscan2.adder(resultList);
//
//        List<LabelPoint> points = new ArrayList<>();
//        for (String labelpoint : finalResultList) {
//            String[] parts = labelpoint.split(",");
//            LabelPoint point = new LabelPoint(
//                    Double.parseDouble(parts[0]),
//                    Double.parseDouble(parts[1]),
//                    Integer.parseInt(parts[2]),
//                    Integer.parseInt(parts[3]),
//                    Long.parseLong(parts[4]),
//                    parts[5]
//            );
//            points.add(point);
//        }
////        System.out.println(finalResultList);
//
//        String outputFilePath2 = "D:\\1HZA\\YJSBYSJ\\Code\\Algorithm\\IncreDbscantwicejdbcpro\\result\\ec_dbscan2new20180430_10_11jdbc.csv";
//        saveToCSV2(points, outputFilePath2);
//*******************************3.主要程序运行完毕，输出计时器结果************************
        long endTime = System.currentTimeMillis(); // 记录结束时间
        long totalTime = endTime - startTime; // 计算运行时长
        long minutes = (totalTime / 1000) / 60; // 将毫秒转换为分钟
        long seconds = (totalTime / 1000) % 60; // 计算剩余的秒数
        System.out.println("程序运行时长：" + minutes + " 分 " + seconds + " 秒");

    }
    private static void saveToCSV1(List<SinglePoint> resultList, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // 写入CSV文件头部
            writer.writeNext(new String[]{"COG", "SOG", "Timestamp", "Longitude", "Latitude", "MMSI", "RouteName", "Type"});

            // 写入数据
            for (SinglePoint point : resultList) {
                String[] data = {
                        String.valueOf(point.getCog()),
                        String.valueOf(point.getSog()),
                        String.valueOf(point.getTimestamp()),
                        String.valueOf(point.getLongitude()),
                        String.valueOf(point.getLatitude()),
                        String.valueOf(point.getMMSI()),
                        String.valueOf(point.getName()),
                        String.valueOf(point.getType())
                };
                writer.writeNext(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveToCSV2(List<LabelPoint> points, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // 写入CSV文件头部
            writer.writeNext(new String[]{ "Latitude", "Longitude","Cluster", "Core", "MMSI","Time"});

            // 写入数据
            for (LabelPoint point : points) {
                String[] data = {
                        String.valueOf(point.getLatitude()),
                        String.valueOf(point.getLongitude()),
                        String.valueOf(point.getCluster()),
                        String.valueOf(point.getCategory()),
                        String.valueOf(point.getMMSI()),
                        String.valueOf(point.getTime())
                };
                writer.writeNext(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
