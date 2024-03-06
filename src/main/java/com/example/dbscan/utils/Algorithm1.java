package com.example.dbscan.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    String[] Linearr;

//    public List<String[]> PointLog = new ArrayList<>();

//    int minpts = 10;// neibors num（最小邻域点数）
//    double radius = 10000;// distance（聚类半径为radius）70
    public int minpts;// neibors num（最小邻域点数）
    public double radius;// distance（聚类半径为radius）70

    /*public Algorithm1(String timestamp)
    {
        SimpleDateFormat ft=new SimpleDateFormat("yyyy/MM/dd HH:mm");
        try{
            this.Timestampstart=ft.parse(timestamp);
        }catch (ParseException e){}
    }*/


    public void URE(String mmsi, String timestamp, String latitude, String longitude, String sog, String cog, String type) {
        try {
            System.out.println("接收到的DBCSAN参数是："+minpts+"和" + radius);

            Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
            if (sum == 100000) {
                sum = 0;
                for (int i = 0; i < vessels.size(); i++) {
                    if ((time.getTime() / 1000 - vessels.get(i).lastupdate.getTime() / 1000) >= 60 * 60 * 3 && vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).visited != true) {
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
                    vessels.get(i).Avgspeed = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).euclidDist(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2)) / dt;
                    //vessels.get(i).Avgspeed = vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).haversineDistance(vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 2)) / dt;
                    if (vessels.get(i).Avgspeed < minspeed && vessels.get(i).status == "sailing") {
                        vessels.get(i).tracks.get(vessels.get(i).tracks.size() - 1).classed = true;
                        vessels.get(i).status = "stationary";
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


}
