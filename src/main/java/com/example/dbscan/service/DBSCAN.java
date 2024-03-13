package com.example.dbscan.service;

import com.example.dbscan.pojo.SinglePoint;
import com.example.dbscan.utils.Algorithm1;
import java.util.Collections;
;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import weka.classifiers.lazy.IBk;
import weka.core.*;
import weka.core.neighboursearch.LinearNNSearch;
import java.util.ArrayList;

public class DBSCAN {
    public int calculate=0;
    public List<String[]> PointLog = new ArrayList<>();

    private Quadtree quadTree;
    private GeometryFactory geomFactory;

    public DBSCAN() {
        this.quadTree = new Quadtree();
        this.geomFactory = new GeometryFactory();
        this.PointLog = new ArrayList<>();
    }


    Algorithm1 algorithm1 = new Algorithm1();
    public List<SinglePoint> DBscanlist() {

        List<SinglePoint> list = new ArrayList<>();

        // PostgreSQL数据库连接信息
        String jdbcUrl = "jdbc:postgresql://localhost:5432/NewAISClean";
        String username = "postgres";
        String password = "postgres";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL查询语句
            String sql = "SELECT mmsi, time, lon, lat, course, speed, status FROM  \"new20180430_08_16\"";
//            String sql = "SELECT mmsi, time, mercator_x, mercator_y, course, speed, status FROM  \"new20180430_08_16\"";
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

//                    handleNewData(MMSI, timestamp, latitude, longitude, sog, cog, type);//计算DBSCAN参数并调用URE
                    handleNewDataQUAD(MMSI, timestamp, latitude, longitude, sog, cog, type);//新版的参数计算，加入了四叉树
//                    handleNewDataQUADKnn(MMSI, timestamp, latitude, longitude, sog, cog, type);//用KNN计算

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
        //*****************************************聚类半径radius计算过程*********************************************
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
        //*****************************************聚类半径radius计算结束*********************************************
        //*****************************************聚类最小阈值minpts计算开始******************************************
        // 确定 minpts，通过寻找距离的最大突变点之前的点数
        double maxDistanceJump = 0;
        int maxJumpIndex = -1;
        for (int i = 1; i < distances.size(); i++) {
            double distanceJump = distances.get(i) - distances.get(i - 1);
            if (distanceJump > maxDistanceJump) {
                maxDistanceJump = distanceJump;
                maxJumpIndex = i;
            }
        }

        // 设置minpts为最大距离跳变点之前的点数
        int minpts = maxJumpIndex + 1;

        // 这里你可以根据需要设置minpts的最小值，以避免它太小
        minpts = Math.max(minpts, 3); // 确保minpts至少为3
        //*****************************************聚类最小阈值minpts计算结束******************************************


        // 更新DBSCAN参数
        algorithm1.radius = radius;
        algorithm1.minpts = minpts;

        // 调用URE方法
//        System.out.println("调用URE方法，新radius值为: " + radius + "新的minpts是" + algorithm1.minpts);
        algorithm1.URE(MMSI, timestamp, latitude, longitude, sog, cog, type);
        calculate++;
        System.out.println("现在是第"+calculate+"次调用URE");
    }

    private void handleNewDataQUAD(String MMSI, String timestamp, String latitude, String longitude, String sog, String cog, String type) {
        // 将传入的参数保存在数组中并添加到PointLog中
        String[] params = new String[]{MMSI, timestamp, latitude, longitude, sog, cog, type};
        PointLog.add(params);

        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        Point newPoint = geomFactory.createPoint(new Coordinate(lon, lat));

        // 添加到Quadtree
        quadTree.insert(new Envelope(new Coordinate(lon, lat)), newPoint);

        // 构建查询的包围盒
        Envelope searchEnv = new Envelope(new Coordinate(lon, lat));
        searchEnv.expandBy(0.02); // 调整值以匹配数据的实际分布

        // 查询附近的点
        List<?> queryResults = quadTree.query(searchEnv);
        System.out.println("本次包围盒包含的点数为: " + queryResults.size());

        List<Double> distances = new ArrayList<>();
        for (Object item : queryResults) {
            Point candidate = (Point) item;
            if (!candidate.equalsExact(newPoint)) {
                double distance = calculateEuclideanDistance(lat, lon, candidate.getY(), candidate.getX());
                distances.add(distance);
            }
        }

        // 对距离进行排序
        Collections.sort(distances);
//        //***********************************************按距离增长差值选择radius********************************
//        // 计算每两个连续距离之间的差值
//        List<Double> distanceDifferences = new ArrayList<>();
//        for (int i = 1; i < distances.size(); i++) {
//            double distanceDifference = distances.get(i) - distances.get(i - 1);
//            distanceDifferences.add(distanceDifference);
//        }
//
//        // 找到差值最大的点并计算radius
//        double radius = 1500; // 默认距离
//        if (!distanceDifferences.isEmpty()) {
//            double maxDistanceChange = Collections.max(distanceDifferences);
//            int indexMaxChange = distanceDifferences.indexOf(maxDistanceChange) + 1; // 加1因为差分后的数组比原数组少一个元素
//            if (indexMaxChange < distances.size()) {
//                radius = distances.get(indexMaxChange); // 更新radius为实际计算值
//            }
//        }
//        int minpts = 4; // 这里的minpts可以保持不变或根据需要调整
//        //***********************************************按距离增长差值选择radius********************************

        //***********************************************按距离增长率选择radius********************************

        // 计算每两个连续距离之间的增长率
        List<Double> growthRates = new ArrayList<>();
        for (int i = 1; i < distances.size(); i++) {
            double previousDistance = distances.get(i - 1);
            double currentDistance = distances.get(i);
            double growthRate = (currentDistance - previousDistance) / previousDistance;
            growthRates.add(growthRate);
        }

        // 找到增长率最大的突变点
        double maxGrowthRateChange = 0;
        int minpts = 4;
        double radius = 0;
        for (int i = 1; i < growthRates.size(); i++) {
            double growthRateChange = growthRates.get(i) - growthRates.get(i - 1);
            if (growthRateChange > maxGrowthRateChange) {
                maxGrowthRateChange = growthRateChange;
                radius = distances.get(i);
//                minpts = i + 1; // 增长率最大突变点的索引+1作为minpts
            }
        }
        //***********************************************按距离增长率选择radius********************************
        // 更新DBSCAN参数
        algorithm1.radius = radius ;
        algorithm1.minpts = minpts ;

        // 调用URE方法
//        System.out.println("调用URE方法，新radius值为: " + radius + "新的minpts是" + algorithm1.minpts);
        algorithm1.URE(MMSI, timestamp, latitude, longitude, sog, cog, type);
        calculate++;
        System.out.println("现在是第"+calculate+"次调用URE");
    }

    private void handleNewDataQUADKnn(String MMSI, String timestamp, String latitude, String longitude, String sog, String cog, String type){
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        Point newPoint = geomFactory.createPoint(new Coordinate(lon, lat));

        // 添加到Quadtree和PointLog
        quadTree.insert(new Envelope(new Coordinate(lon, lat)), newPoint);
        String[] params = new String[]{MMSI, timestamp, latitude, longitude, sog, cog, type};
        PointLog.add(params);


        // 构建查询的包围盒
        Envelope searchEnv = new Envelope(new Coordinate(lon, lat));
        searchEnv.expandBy(1000); // 调整值以匹配数据的实际分布
        // 查询附近的点
        List<Point> queryResults = quadTree.query(searchEnv);

        System.out.println("本次包围盒包含的点数为: " + queryResults.size());
        // 准备数据
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("longitude"));
        attributes.add(new Attribute("latitude"));
        Instances dataset = new Instances("QueryResults", attributes, queryResults.size());


        // 填充数据
        for (Point point : queryResults) {
            double[] values = new double[dataset.numAttributes()];
            values[0] = point.getCoordinate().x; // 经度
            values[1] = point.getCoordinate().y; // 纬度
            dataset.add(new DenseInstance(1.0, values));
        }
        dataset.setClassIndex(-1); // 无分类属性
        // 使用IBk
        int K = 6;
        IBk knn = new IBk(K);

        // 添加一个名为"class"的假类属性（如果还没有添加的话）
        if (dataset.attribute("class") == null) {
            dataset.insertAttributeAt(new Attribute("class"), dataset.numAttributes());
            for (int i = 0; i < dataset.numInstances(); i++) {
                dataset.instance(i).setValue(dataset.numAttributes() - 1, 0.0); // 给假类属性设置值
            }
            dataset.setClassIndex(dataset.numAttributes() - 1); // 设置这个新属性为类属性
        }

        try {
            knn.buildClassifier(dataset); // 根据数据集建立KNN模型

            // 使用LinearNNSearch进行搜索，这里可以根据需要选择不同的搜索算法
            LinearNNSearch search = new LinearNNSearch(dataset);
            knn.setNearestNeighbourSearchAlgorithm(search);

            ArrayList<Double> kDistances = new ArrayList<>(); // 存储所有点的第K个最近邻的距离

            // 对于数据集中的每个点计算其K个最近邻并存储第K个最近邻的距离
            for (int i = 0; i < dataset.numInstances(); i++) {
                Instances nearestInstances = search.kNearestNeighbours(dataset.instance(i), K);
                double[] distances = search.getDistances();
                double kthDistance = distances[distances.length - 1]; // 第K个最近邻的距离
                kDistances.add(kthDistance);
            }

            // 对所有的第K个最近邻的距离进行排序
            Collections.sort(kDistances);

            // 寻找距离的最大增量，认为是拐点
            double maxDelta = 0;
            double radius = 0; // 这将是我们找到的拐点对应的距离值
            int minpts = 6;
            for (int i = 1; i < kDistances.size(); i++) {
                double delta = kDistances.get(i) - kDistances.get(i - 1);
                if (delta > maxDelta) {
                    maxDelta = delta;
                    radius = kDistances.get(i);
                }
            }
            // 更新radius值
            algorithm1.radius = radius;
            algorithm1.minpts = minpts ;
            // 调用URE方法

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private double calculateEuclideanDistance(double x1, double y1, double x2, double y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

}


