package com.example.dbscan.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

//Unsupervised Route Extraction
public class Algorithm1 {
    public WayPointclass ENs = new WayPointclass("ENs");
    public WayPointclass POs = new WayPointclass("POs");
    public WayPointclass EXs = new WayPointclass("EXs");
    public List<Vessel> vessels = new ArrayList<>();
    public List<Route> routes = new ArrayList<>();
    public ArrayList<Integer> mmsid = new ArrayList<Integer>();
    double minspeed = 5;
    //double dietadays;
    Date Timestampstart;
    Date Timestampnow;
    int sum = 0;
    int sum1 = 1;
    double tao = 60 * 60 * 6;
    public int UREcalculate=0;//计数器，记录URE被调用的次数
    public int IncDBcalculate = 0;//计数器，记录IncDBSCAN被调用的次数
    public List<String[]> PointLog = new ArrayList<>();//保存读入的点数据，作为参数计算基础

    private Quadtree quadTree;
    private GeometryFactory geomFactory;

    public Algorithm1() {
        this.quadTree = new Quadtree();
        this.geomFactory = new GeometryFactory();
    }


//    int minpts = 10;// neibors num（最小邻域点数）
//    double radius = 10000;// distance（聚类半径为radius）70
    public int minpts = 4;// neibors num（最小邻域点数）
    public double radius = 20000;// distance（聚类半径为radius）70

    /*public Algorithm1(String timestamp)
    {
        SimpleDateFormat ft=new SimpleDateFormat("yyyy/MM/dd HH:mm");
        try{
            this.Timestampstart=ft.parse(timestamp);
        }catch (ParseException e){}
    }*/


    public void URE(String mmsi, String timestamp, String latitude, String longitude, String sog, String cog, String type) {
        try {
//            System.out.println("接收到的DBCSAN参数是："+minpts+"和" + radius);
            UREcalculate++;
//            System.out.println("第" + UREcalculate + "次调用URE");
            Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
            if (sum == 100000) {
                sum = 0;
                for (int i = 0; i < vessels.size(); i++) {
                    if ((time.getTime() / 1000 - vessels.get(i).lastupdate.getTime() / 1000) >= 60 * 60 * 3 && vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).visited != true) {
                        //调用DBCAN参数计算程序
                        parammeterCalculation(mmsi, timestamp, latitude, longitude, sog, cog, type);
                        IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
                        incCluster.incrementalUpdate(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1), EXs, vessels, routes);

                        if (incCluster.flag == 1) {
                            //vessels.get(i).tracks.get(vessels.get(i).tracks.size()-1).clusterIndex=POs.points.get(POs.points.size()-1).clusterIndex;
                            vessels.get(i).wps.add("EXs" + EXs.points.get(EXs.points.size() - 1).clusterIndex);
                            vessels.get(i).timestampwps.add(EXs.points.get(EXs.points.size() - 1));
                            Algorithm3 algorithm3 = new Algorithm3();
                            algorithm3.ROM(vessels.get(i), routes);
                        }
                    }
                }
            }

        } catch (ParseException e) {
        }
        sum1++;
        sum++;
        int mid;
        mid = Integer.valueOf(mmsi);
        if (mmsid.contains(mid) == false) {
            mmsid.add(mid);
            Vessel vessel = new Vessel(mid);
            Point point = new Point(mmsi, timestamp, latitude, longitude, sog, cog, type);
            point.classed = true;
            vessel.tracks.add(point);
            vessel.status = "sailing";
            vessel.Avgspeed = point.sog;
            vessel.lastupdate = point.timestamp;
            vessels.add(vessel);
            //调用DBCAN参数计算程序
            parammeterCalculation(mmsi, timestamp, latitude, longitude, sog, cog, type);
            IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
            incCluster.incrementalUpdate(vessels.get(vessels.size() - 1).tracks.get(vessels.get(vessels.size() - 1).tracks.size() - 1), ENs, vessels, routes);

            if (incCluster.flag == 1) {
                //vessels.get(vessels.size()-1).tracks.get(vessels.get(vessels.size()-1).tracks.size()-1).clusterIndex=ENs.points.get(ENs.points.size()-1).clusterIndex;
                vessels.get(vessels.size() - 1).wps.add("ENs" + ENs.points.get(ENs.points.size() - 1).clusterIndex);
                vessels.get(vessels.size() - 1).timestampwps.add(ENs.points.get(ENs.points.size() - 1));
                Algorithm3 algorithm3 = new Algorithm3();
                algorithm3.ROM(vessels.get(vessels.size() - 1), routes);
            }
        } else {
            for (int i = 0; i < vessels.size(); i++) {
                if (vessels.get(i).MMSI == mid) {
                    Point point = new Point(mmsi, timestamp, latitude, longitude, sog, cog, type);
                    vessels.get(i).tracks.add(point);
                    //vessels.get(i).Avgspeed=point.sog;
                    vessels.get(i).lastupdate = point.timestamp;
                    Date time1 = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).timestamp;
                    Date time2 = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2).timestamp;
                    int dt = (int) (time1.getTime() - time2.getTime());
                    //vessels.get(i).Avgspeed = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).euclidDist(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2)) / dt;
                    vessels.get(i).Avgspeed = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).haversineDistance(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2)) / dt;
                    if (vessels.get(i).Avgspeed < minspeed && vessels.get(i).status == "sailing") {
                        vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).classed = true;
                        vessels.get(i).status = "stationary";
                        //调用DBCAN参数计算程序
                        parammeterCalculation(mmsi, timestamp, latitude, longitude, sog, cog, type);
                        IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
                        incCluster.incrementalUpdate(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1), POs, vessels, routes);

                        if (incCluster.flag == 1) {
                            //vessels.get(i).tracks.get(vessels.get(i).tracks.size()-1).clusterIndex=POs.points.get(POs.points.size()-1).clusterIndex;
                            vessels.get(i).wps.add("POs" + POs.points.get(POs.points.size() - 1).clusterIndex);
                            vessels.get(i).timestampwps.add(POs.points.get(POs.points.size() - 1));
                            Algorithm3 algorithm3 = new Algorithm3();
                            algorithm3.ROM(vessels.get(i), routes);
                        }
                    }
                   /* if(vessel.status=="lost")
                    {
                        vessel.status="sailing";
                        IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts,POs,vessels,routes);
                        incCluster.incrementalUpdate(point);
                        ENs.points=incCluster.points;
                        vessels=incCluster.vessels;
                        routes=incCluster.routes;
                        Algorithm3 algorithm3=new Algorithm3(vessel,routes);
                        algorithm3.ROM();
                        vessel=algorithm3.vessel;
                        routes=algorithm3.routes;
                    }*/
                    /*if((point.timestamp.getTime()/1000-vessels.get(i).tracks.get(vessels.get(i).tracks.size()-2).timestamp.getTime()/1000)>=tao)
                    {
                        vessels.get(i).status="sailing";
                        if(vessels.get(i).tracks.get(vessels.get(i).tracks.size()-2).classed!=true)
                       {
                            IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
                            incCluster.incrementalUpdate(vessels.get(i).tracks.get(vessels.get(i).tracks.size()-2),EXs,vessels,routes);
                            if(incCluster.flag==1)
                             {
                                vessels.get(i).tracks.get(vessels.get(i).tracks.size()-2).clusterIndex=EXs.points.get(EXs.points.size()-2).clusterIndex;
                                vessels.get(i).wps.add("EXs"+EXs.points.get(EXs.points.size()-2).clusterIndex);
                                vessels.get(i).timestampwps.add(EXs.points.get(EXs.points.size()-2));
                                Algorithm3 algorithm31=new Algorithm3();
                                algorithm31.ROM(vessels.get(i),routes);
                            }
                        }
                        //lost------sailing
                        IncDBSCANCluster incCluster1 = new IncDBSCANCluster(radius, minpts);
                        incCluster1.incrementalUpdate(vessels.get(i).tracks.get(vessels.get(i).tracks.size()-1),ENs,vessels,routes);
                        if(incCluster1.flag==1)
                        {
                            vessels.get(i).tracks.get(vessels.get(i).tracks.size()-1).clusterIndex=ENs.points.get(ENs.points.size()-1).clusterIndex;
                            vessels.get(i).wps.add("ENs"+ENs.points.get(ENs.points.size()-1).clusterIndex);
                            vessels.get(i).timestampwps.add(ENs.points.get(EXs.points.size()-1));
                            Algorithm3 algorithm3=new Algorithm3();
                            algorithm3.ROM(vessels.get(i),routes);
                        }
                    }*/
                }
            }
        }

    }

    private void parammeterCalculation(String MMSI, String timestamp, String latitude, String longitude, String sog, String cog, String type) {
        // 将传入的参数保存在数组中并添加到PointLog中
        String[] params = new String[]{MMSI, timestamp, latitude, longitude, sog, cog, type};
        PointLog.add(params);

        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        org.locationtech.jts.geom.Point newPoint = geomFactory.createPoint(new Coordinate(lon, lat));

        // 添加到Quadtree
        quadTree.insert(new Envelope(new Coordinate(lon, lat)), newPoint);

        // 构建查询的包围盒
        Envelope searchEnv = new Envelope(new Coordinate(lon, lat));
        searchEnv.expandBy(1500); // 调整值以匹配数据的实际分布

        // 查询附近的点
        List<?> queryResults = quadTree.query(searchEnv);
        System.out.println("本次包围盒包含的点数为: " + queryResults.size());

        List<Double> distances = new ArrayList<>();
        for (Object item : queryResults) {
            org.locationtech.jts.geom.Point candidate = (org.locationtech.jts.geom.Point) item;
            if (!candidate.equalsExact(newPoint)) {
                double distance = calculateHaversine(lat, lon, candidate.getY(), candidate.getX());
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
        this.radius = radius ;
        this.minpts = minpts ;
        IncDBcalculate++;
        System.out.println("第"+IncDBcalculate+"次调用IncDBSCAN，计算得到的参数radius = " + this.radius+ ",minpts = " + this.minpts);

    }

    private double calculateHaversine (double lat1, double lon1, double lat2, double lon2) {
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

    private double calculateEuclidean(double x1, double y1, double x2, double y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }


}
