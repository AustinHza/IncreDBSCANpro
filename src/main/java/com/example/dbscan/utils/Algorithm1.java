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

    public int ENScal = 0;
    public int EXScal = 0;
    public int POScal = 0;

    private Quadtree quadTree;
    private GeometryFactory geomFactory;

    public Algorithm1() {
        this.quadTree = new Quadtree();
        this.geomFactory = new GeometryFactory();
    }


//    int minpts = 10;// neibors num（最小邻域点数）
//    double radius = 10000;// distance（聚类半径为radius）70
    public int minpts = 10;// neibors num（最小邻域点数）
    public double radius = 10000;// distance（聚类半径为radius）70

    /*public Algorithm1(String timestamp)
    {
        SimpleDateFormat ft=new SimpleDateFormat("yyyy/MM/dd HH:mm");
        try{
            this.Timestampstart=ft.parse(timestamp);
        }catch (ParseException e){}
    }*/


    public void URE(String mmsi, String timestamp, String mercator_x, String mercator_y, String sog, String cog, String type, String latitude, String longitude) {
        try {
//            System.out.println("接收到的DBCSAN参数是："+minpts+"和" + radius);
//            System.out.println("坐标是是："+latitude+"和" + longitude);
            UREcalculate++;
//            System.out.println("第" + UREcalculate + "次调用URE");
            Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
            if (sum == 100000) {
                sum = 0;
                for (int i = 0; i < vessels.size(); i++) {
                    if ((time.getTime() / 1000 - vessels.get(i).lastupdate.getTime() / 1000) >= 60 * 60 * 3 && !vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).visited &&vessels.get(i).status!="lost") {
/*                       这里对应伪代码Al1中26-33行，对每艘船舶，检查当前时间(time)与船舶最后更新时间(vessels.get(i).lastupdate)的差是否大于或等于3小时（60秒 * 60分钟 * 3）。如果是，
                        表示该船舶已经3小时未更新，并且该船舶的最后一条轨迹(tracks)未被访问过，则标为EXS出口点进行相关聚类操作并且把该船只标为lost*/
                        int mid1;
                        mid1 = Integer.valueOf(vessels.get(i).MMSI);
                        Vessel vessel = new Vessel(mid1);
                        vessel.status = "lost";

                        //调用DBCAN参数计算程序
                        parammeterCalculation(mmsi, timestamp, mercator_x, mercator_y, sog, cog, type);
                        IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
                        incCluster.incrementalUpdate(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1), EXs, vessels, routes);
                        EXScal++;
                        System.out.println("EXS数量：" + EXScal);


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
/*            这个if对应伪代码2-8行，如果船舶对象集合中未含有该MMSI所表标识的传播对象，则表示该船泊第一次于监测窗
            口出现，将该 message 所表示的信息加入到 ENs 中，并将此 Vessel 标记为 sailing*/
            mmsid.add(mid);
            Vessel vessel = new Vessel(mid);
            Point point = new Point(mmsi, timestamp, mercator_x, mercator_y, sog, cog, type,latitude,longitude);
            point.classed = true;
            vessel.tracks.add(point);
            vessel.status = "sailing";
            vessel.Avgspeed = point.sog;
            vessel.lastupdate = point.timestamp;
            vessels.add(vessel);
            //调用DBCAN参数计算程序
            parammeterCalculation(mmsi, timestamp, mercator_x, mercator_y, sog, cog, type);
            IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
            incCluster.incrementalUpdate(vessels.get(vessels.size() - 1).tracks.get(vessels.get(vessels.size() - 1).tracks.size() - 1), ENs, vessels, routes);
            ENScal++;
            System.out.println("ENS数量：" + ENScal);
            if (incCluster.flag == 1) {
                //vessels.get(vessels.size()-1).tracks.get(vessels.get(vessels.size()-1).tracks.size()-1).clusterIndex=ENs.points.get(ENs.points.size()-1).clusterIndex;
                vessels.get(vessels.size() - 1).wps.add("ENs" + ENs.points.get(ENs.points.size() - 1).clusterIndex);
                vessels.get(vessels.size() - 1).timestampwps.add(ENs.points.get(ENs.points.size() - 1));
                Algorithm3 algorithm3 = new Algorithm3();
                algorithm3.ROM(vessels.get(vessels.size() - 1), routes);
            }
        } else {
            /* 这个else对应伪代码9-25行，如果船舶对象集合中含有该MMSI所表标识的传播对象，则判断其状态*/
            for (int i = 0; i < vessels.size(); i++) {
                if (vessels.get(i).MMSI == mid) {
                    Point point = new Point(mmsi, timestamp, mercator_x, mercator_y, sog, cog, type,latitude,longitude);
                    vessels.get(i).tracks.add(point);
                    //vessels.get(i).Avgspeed=point.sog;
                    vessels.get(i).lastupdate = point.timestamp;
                    Date time1 = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).timestamp;
                    Date time2 = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2).timestamp;
                    int dt = (int) (time1.getTime() - time2.getTime());
                    //vessels.get(i).Avgspeed = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).euclidDist(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2)) / dt;
                    vessels.get(i).Avgspeed = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).DistanceCalculate(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2)) / dt;
                    if (vessels.get(i).Avgspeed < minspeed && vessels.get(i).status == "sailing") {
/*                      这里对应10-18行如果航速小于最小值且船舶更新前的状态为sailing 则当前message满足停泊点集合POs的条件*/
                        vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).classed = true;
                        vessels.get(i).status = "stationary";
                        //调用DBCAN参数计算程序
                        parammeterCalculation(mmsi, timestamp, mercator_x, mercator_y, sog, cog, type);
                        IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
                        incCluster.incrementalUpdate(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1), POs, vessels, routes);
                        POScal++;
                        System.out.println("POs数量：" + POScal);

                        if (incCluster.flag == 1) {
                            //vessels.get(i).tracks.get(vessels.get(i).tracks.size()-1).clusterIndex=POs.points.get(POs.points.size()-1).clusterIndex;
                            vessels.get(i).wps.add("POs" + POs.points.get(POs.points.size() - 1).clusterIndex);
                            vessels.get(i).timestampwps.add(POs.points.get(POs.points.size() - 1));
                            Algorithm3 algorithm3 = new Algorithm3();
                            algorithm3.ROM(vessels.get(i), routes);
                        }
                    }
                    if(vessels.get(i).status=="lost")
                   //这里应该对应的是19-25行
                    {
                        vessels.get(i).status="sailing";
                        //调用DBCAN参数计算程序
                        parammeterCalculation(mmsi, timestamp, mercator_x, mercator_y, sog, cog, type);
                        IncDBSCANCluster incCluster = new IncDBSCANCluster(radius, minpts);
                        incCluster.incrementalUpdate(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1), ENs, vessels, routes);
                        ENScal++;
                        System.out.println("ENs数量：" + ENScal);

                        if (incCluster.flag == 1) {
                            //vessels.get(i).tracks.get(vessels.get(i).tracks.size()-1).clusterIndex=POs.points.get(POs.points.size()-1).clusterIndex;
                            vessels.get(i).wps.add("ENs" + ENs.points.get(ENs.points.size() - 1).clusterIndex);
                            vessels.get(i).timestampwps.add(ENs.points.get(ENs.points.size() - 1));
                            Algorithm3 algorithm3 = new Algorithm3();
                            algorithm3.ROM(vessels.get(i), routes);
                        }
//                        ENs.points=incCluster.points;
//                        vessels=incCluster.vessels;
//                        routes=incCluster.routes;
//                        Algorithm3 algorithm3=new Algorithm3(vessel,routes);
//                        algorithm3.ROM();
//                        vessel=algorithm3.vessel;
//                        routes=algorithm3.routes;
                    }
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

    private void parammeterCalculation(String MMSI, String timestamp, String mercator_x, String mercator_y, String sog, String cog, String type) {
        // 将传入的参数保存在数组中并添加到PointLog中
        String[] params = new String[]{MMSI, timestamp, mercator_x, mercator_y, sog, cog, type};
        PointLog.add(params);

        double mercatorx = Double.parseDouble(mercator_x);
        double mercatory = Double.parseDouble(mercator_y);
        org.locationtech.jts.geom.Point newPoint = geomFactory.createPoint(new Coordinate(mercatorx, mercatory));

        // 添加到Quadtree
        quadTree.insert(new Envelope(new Coordinate(mercatorx, mercatory)), newPoint);
//        // 创建一个大到足以覆盖整个四叉树的 Envelope
//        Envelope bigEnvelope = new Envelope(
//                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
//                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        // 构建查询的包围盒
        Envelope searchEnv = new Envelope(new Coordinate(mercatorx, mercatory));
        searchEnv.expandBy(10000); // 调整值以匹配数据的实际分布

        // 查询附近的点
        List<?> queryResults = quadTree.query(searchEnv);
        System.out.println("本次包围盒包含的点数为: " + queryResults.size());

        List<Double> distances = new ArrayList<>();
        for (Object item : queryResults) {
            org.locationtech.jts.geom.Point candidate = (org.locationtech.jts.geom.Point) item;
            if (!candidate.equalsExact(newPoint)) {
                double distance = calculateEuclidean(mercatorx, mercatory, candidate.getX(), candidate.getY());
                distances.add(distance);
            }
        }

        // 对距离进行排序
        Collections.sort(distances);
//        //***********************************************按距离增长差值选择radius********************************
        double radius = 10000; // 默认距离

        // 只有当列表中有足够的数据时，才进行斜率计算
        if (distances.size() > 1) {
            // 计算每两个连续距离之间的斜率
            List<Double> slopes = new ArrayList<>();
            for (int i = 1; i < distances.size(); i++) {
                slopes.add(distances.get(i) - distances.get(i - 1));
            }

            // 找到斜率最大的索引
            double maxSlope = -1;
            int maxSlopeIndex = -1;
            for (int i = 0; i < slopes.size(); i++) {
                if (slopes.get(i) > maxSlope) {
                    maxSlope = slopes.get(i);
                    maxSlopeIndex = i;
                }
            }

            // 拐点在原始距离列表中的位置是 maxSlopeIndex + 1
            int turningPointIndex = maxSlopeIndex + 1;

            // 检查拐点索引是否在距离列表范围内
            if (turningPointIndex < distances.size()) {
                // 获取拐点对应的距离值作为radius
                radius = distances.get(turningPointIndex);
            }
        }
//        //***********************************************按距离增长差值选择radius********************************

        //***********************************************按距离增长率选择radius********************************
//
//        // 计算每两个连续距离之间的增长率
//        List<Double> growthRates = new ArrayList<>();
//        for (int i = 1; i < distances.size(); i++) {
//            double previousDistance = distances.get(i - 1);
//            double currentDistance = distances.get(i);
//            // 防止除以零
//            if (previousDistance != 0) {
//                double growthRate = (currentDistance - previousDistance) / previousDistance;
//                growthRates.add(growthRate);
//            }
//        }
//
//        // 找到增长率最大的突变点
//        int maxGrowthRateIndex = 0;
//        double maxGrowthRateChange = 0;
//        for (int i = 1; i < growthRates.size(); i++) {
//            double previousGrowthRate = growthRates.get(i - 1);
//            double currentGrowthRate = growthRates.get(i);
//            double growthRateChange = Math.abs(currentGrowthRate - previousGrowthRate);
//            if (growthRateChange > maxGrowthRateChange) {
//                maxGrowthRateChange = growthRateChange;
//                maxGrowthRateIndex = i;
//            }
//        }
//
//        // 使用突变点之后的第一个距离作为radius
//        double radius = distances.isEmpty() ? 0 : distances.get(Math.min(maxGrowthRateIndex + 1, distances.size() - 1));
//        // 如果radius为0，使用默认值10000
//        if (radius == 0) {
//            radius = 10000;
//        }
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
