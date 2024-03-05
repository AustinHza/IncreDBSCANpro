package com.example.dbscan.MyJava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Administrator on 2020/5/6.
 */
public class MyJavaClass {

    static ArrayList<Integer>mmsid = new ArrayList<Integer>();	//存储所有的MMSID号
    static int[] pointMmsi; // index(MMSI对应点的索引范围：注意开始索引)
    static Point1 []p;												// Array of point objects

    // parameter:store trajectory
    static List<String> pId;// all points index点的id号
    static List<Integer> firstPointIndex;// 第一个点的指标数
    static List<Integer> lastPointIndex;// 最后一个点的指标数
    static List<Double> azimuth;
    static List<Double> loxodrome;
    static int trajectoryTotalNum;// 轨迹的总数
    static int[] trajectoryMmsi;// mmsi corresponding trajectory's index range(MMSI对应轨迹指数范围)
    static Trajectory []traj; //储存所有的轨迹

    // parameter:segmentation threshold(参数：分割阈值)
    static double aziRequirement = 0.000001;// angle转角阈值
    // parameter: earth
    static double a = 6378137;// a为地球长半径
    static double e2 = 0.0066943799013f;// e2为第一偏心率的平方
    //DBSCAN参数
    static int minpts =10;// neibors num（最小邻域点数）
    static double radius = 10000;// distance（聚类半径为radius）


    public List adder(List labeledPoints_list){
        System.out.println("MyDBSCAN开始！！！其中minpits="+minpts+",radius="+radius);

        /******************************************************************************************************
         * 按mmsid存储点
         *******************************************************************************************************/

        int mid;  //临时存储各个点的MMSID号
        int len=labeledPoints_list.size();  //点集合的长度，即点的数量
        System.out.println("数据长度："+len);
        p=new Point1[len];//初始化p

        for (int i = 0; i < len; i++) {
            String s = labeledPoints_list.get(i).toString();
//            System.out.println("s="+s+"i="+i);
            mid=Integer.valueOf(s.split(",")[5].replace("",""));
            if (mmsid.contains(mid)==false)
                mmsid.add(mid);
        }
        Collections.sort(mmsid);
       // System.out.println("mmsid"+mmsid);

        pointMmsi = new int[mmsid.size() + 1];//初始化
        pointMmsi[0] = 0;
        for (int h = 0; h < mmsid.size(); h++) {
            int j = pointMmsi[h];
            for(int i=0;i<len;i++){
                mid=Integer.valueOf(labeledPoints_list.get(i).toString().split(",")[5].replace(" ",""));

                if(mid==mmsid.get(h)){
                    p[j]=new Point1(labeledPoints_list.get(i).toString());
                        j++;
                }
            }
            pointMmsi[h + 1] = j;

        }
        System.out.println(p.length+" "+p[0].latitude+" "+p[0].longitude);


        /************************************************************************************************
         * 分割轨迹
         ************************************************************************************************/

        // initialize trajectory parameter(初始化轨迹参数)
        pId = new ArrayList<String>();
        firstPointIndex = new ArrayList<Integer>();
        lastPointIndex = new ArrayList<Integer>();
        azimuth = new ArrayList<Double>();
        loxodrome = new ArrayList<Double>();
        trajectoryMmsi=new int[mmsid.size()];
        //////////
        // trajectory segmentation（轨迹分割）
        /////////
        for (int i = 0; i < mmsid.size(); i++) {
            int startPosition = pointMmsi[i];
            int endPosition = pointMmsi[i + 1];
            if (endPosition - startPosition <= 1)
                continue;
            for (int j = startPosition; j < endPosition - 1; j++) {
                // trajectory segmentation
                double[] aziLox = getAziLox(p[j].longitude, p[j].latitude, p[j+1].longitude,p[j+1].latitude);
                double azi = aziLox[0];// radian（弧度）
                double lox = aziLox[1];// meter（米）
                String strpIndex = Integer.toString(j) + "," + Integer.toString(j + 1);
                int fPIndex = j;
                int lPIndex = j + 1;
                int size = pId.size();
                if (j == startPosition) {
                    trajectoryMmsi[i] = size;
                    //System.out.println(trajectoryMmsi[i]);
                }
                else {// if(j!=startPosition)
                    double aziDiffer = Math.abs(azi - azimuth.get(size - 1));
                    if (aziDiffer < aziRequirement) {// don't segment（不分割,合为一条）
                        int firstPIndex = firstPointIndex.get(size - 1);
                        aziLox = getAziLox(p[firstPIndex].longitude, p[firstPIndex].latitude, p[j + 1].longitude,
                                p[j + 1].latitude);
                        azi = aziLox[0];
                        lox = aziLox[1];
                        strpIndex = pId.get(size - 1) + "," + Integer.toString(j + 1);
                        fPIndex = firstPIndex;
                        lPIndex = j + 1;
                        pId.remove(size - 1);
                        azimuth.remove(size - 1);
                        loxodrome.remove(size - 1);
                        firstPointIndex.remove(size - 1);
                        lastPointIndex.remove(size - 1);
                    }
                }
                pId.add(strpIndex);
                azimuth.add(azi);
                loxodrome.add(lox);
                firstPointIndex.add(fPIndex);
                lastPointIndex.add(lPIndex);
            }
           // System.out.println("trajectory loading...i=" + i);
        }
        System.out.println("segmentation successful");
      //  System.out.println("pId"+pId);
        trajectoryTotalNum = pId.size();//轨迹总数
        System.out.println("轨迹总数："+trajectoryTotalNum );

        //子轨迹存贮在Trajectory里
        traj=new Trajectory[trajectoryTotalNum];
        for(int k=0;k<trajectoryTotalNum;k++){
            traj[k] = new Trajectory();		// 创建对象
            traj[k].index=k;
            traj[k].pid=pId.get(k);
        }
        System.out.println(traj.length+" "+traj[3].index+" "+traj[3].pid);

        //所有的轨迹点变为子轨迹，采用子轨迹来聚类；
        Trajectory current_trajectory;
        Trajectory result_trajectory;
        int cid	= 0;					// counter for the cluster id
        int to=0;
        ArrayList<Integer>val = new ArrayList<Integer>();  //记录同一个聚类数量

        /************************************************************************************************
         * 开始轨迹DBSCAN聚类
         ************************************************************************************************/
        for(int k=0;k<trajectoryTotalNum;k++)
        {

            current_trajectory = traj[k];

            if (current_trajectory.cluster==0 || current_trajectory.cluster==-1)		// cluster_id is -1 for noise, 0 for unclassified and other values are for cluster id
            {
                // Cluster expansion part

                //System.out.println("Current index ---->"+k);
                try
                {
                    // Finding points in the epsilon neighbourhood of a point
                    List<Integer>seeds = NearestEuclidean(current_trajectory, radius);
                   // System.out.println(seeds);
                    int count=0;
                    for(int f=0;f<seeds.size();f++)
                    {
                        if(traj[seeds.get(f)].cluster_id.equals("0")||traj[seeds.get(f)].cluster_id.equals("-1"))
                            count=count+1;

                    }

                    if((count)<minpts)		// 1 is subtracted because point itself is also included
                    {
                        // no core point
                        //current_trajectory.cluster_id = ""+-1;
                        current_trajectory.cluster = -1;
                        current_trajectory.isCorepoint = false;
                        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                        continue;

                    }
                    else
                    {
                        // if point is a core point
                        // all point in thye seed are density reachable
                        // from the point
                        to=0;
                        cid = cid+1;

                        for(int h=0;h<seeds.size();h++)  // last element is the point itself
                        {
                            int ind = seeds.get(h);
                            if(traj[ind].cluster== 0 || traj[ind].cluster == -1)
                            {
                                //traj[ind].cluster_id ="C"+""+cid;
                                traj[ind].cluster = cid;
                                traj[ind].eps_value = radius;
                                to=to+1;
                            }

                        }
                        current_trajectory.isCorepoint=true;
                        current_trajectory.cluster=cid;
                        //System.out.println(seeds);
                        //seeds.remove(seeds.indexOf(k+1));			// it removes the object with given value from the list

                        while(!(seeds.isEmpty()))
                        {
                            current_trajectory = traj[seeds.get(0)];	// fetching first elemnt from the list
                            List<Integer>result = NearestEuclidean(current_trajectory, radius);

                            if((result.size())>=minpts)
                            {
                                for(int z=0;z<result.size();z++)		// take care of the point itself by subtracting 1
                                {
                                    result_trajectory = traj[result.get(z)];

                                    if(result_trajectory.cluster == 0 || result_trajectory.cluster==-1)
                                    {

                                        if(result_trajectory.cluster==0)
                                            seeds.add(result.get(z));

                                       // result_trajectory.cluster_id = "C"+""+cid;
                                        result_trajectory.cluster = cid;
                                        result_trajectory.eps_value=radius;
                                        to=to+1;


                                    }

                                }
                                current_trajectory.isCorepoint = true;
                                current_trajectory.cluster=cid;

                            }


                            seeds.remove(0);		// removes element at index 0 in seeds list

                        }				// end of while loop

                        //continue;
                        val.add(to);
                    }



                }
                catch (Exception e) {
                    e.printStackTrace();}

            }
            else
                continue;

        }
        // End of DBSCAN part

        //轨迹聚类转化为点聚类

        int ji2=0;
        for (int i = 0; i < trajectoryTotalNum; i++) {

            String[] strPointIndex = traj[i].pid.split(",");
            //if(strPointIndex.length>2)
            //System.out.println(strPointIndex.length);
            for (int j = 0; j < strPointIndex.length - 1; j++) {
                int pIndex = Integer.parseInt(strPointIndex[j]);
                //pointClass[pIndex] = c;
                p[pIndex].cluster=traj[i].cluster;
                p[pIndex].cluster_id=traj[i].cluster_id;
                p[pIndex].isCorepoint=traj[i].isCorepoint;
                ji2++;
            }
        }

        System.out.println("参与聚类点的数量："+ji2);


        //******************************************************************************************************88
        // 聚类结果保存在list里，传递到LocalDBSCANNNaive里
        //*****************************************************************************************************88
        int j=0;
        List<String> labelpoints=new ArrayList<>();

        for (int i = 0; i < len; i++) {
            if (p[i].cluster==0)
                p[i].cluster=-1;
            if (p[i].cluster==-1)
                j++;
            if (p[i].isCorepoint==true)// 1代表是核心点，0代表是边界点
                labelpoints.add(p[i].latitude+","+p[i].longitude+","+p[i].cluster+","+1+","+p[i].mmsid);
            else if (p[i].isCorepoint==false)
                labelpoints.add(p[i].latitude+","+p[i].longitude+","+p[i].cluster+","+0+","+p[i].mmsid);

        }
        System.out.println("聚类数目："+val.size());
        System.out.println("噪声结果的大小："+j);
       // System.out.println(labelpoints.get(1000));

        System.out.println("MyDBSCAN结束！！！");
        return labelpoints;
    }




    // Finding trajectorys in the epsilon neighbourhood of a trajectory
    public static List<Integer> NearestEuclidean(Trajectory current_trajectory , double radius){
        List<Integer> seeds = new ArrayList<Integer>();

        for (int i = 0; i < trajectoryTotalNum; i++) {
            if(i!=current_trajectory.index)//去除自己
            {
                double D = getStructureDistance(current_trajectory.index, i);
                if (D < radius) {
                    seeds.add(i);
                }
            }

        }
        return seeds;
    }

    // compute getAziLox（计算求出轨迹恒向线）
    public static double[] getAziLox(double longitude1, double latitude1, double longitude2, double latitude2) {
        double B1 = degree2rad(latitude1);
        double B2 = degree2rad(latitude2);
        double Bd = B2 - B1;

        double L1 = degree2rad(longitude1);
        double L2 = degree2rad(longitude2);
        double Ld = L2 - L1;
        while (Ld > Math.PI) {
            Ld = Ld - 2 * Math.PI;
        }
        while (Ld < -Math.PI) {
            Ld = Ld + 2 * Math.PI;
        }

        double azi = 0;
        double lox = 0;// Sb,loxodrome（斜航线）
        // System.out.println("longitude1:"+longitude1+",latitude1:"+latitude1+",longitude2:"+longitude2+",latitude2:"+latitude2);
        if (Bd == 0) {
            azi = Math.PI / 2;
            double sinB = Math.sin(B1);
            double cosB = Math.cos(B1);
            double r = a * cosB / Math.sqrt(1 - e2 * sinB * sinB);
            lox = r * Math.abs(Ld);
            // System.out.println("Ld:"+Ld+",Bd:"+Bd+",azi:"+azi+",r:"+r+",lox:"+lox);
        } else {
            double q1 = getQ(B1);
            double q2 = getQ(B2);

            double xd = a * Ld;
            double yd = a * (q2 - q1);
            azi = Math.atan2(xd, yd);
            if (azi < 0) {
                azi = azi + 2 * Math.PI;
            }
            double SB1 = getSB(B1);
            double SB2 = getSB(B2);
            lox = ((SB2 - SB1) / Math.cos(azi));
            // System.out.println("Ld:"+Ld+",Bd:"+Bd+",azi:"+azi+",SB1:"+SB1+",SB2:"+SB2+",lox:"+lox);
        }
        double[] aziLox = new double[2];
        aziLox[0] = azi;
        aziLox[1] = lox;
        return aziLox;
    }

    // compute q（计算 q）
    public static double getQ(double B) {
        double e = Math.sqrt(e2);
        double sinB = Math.sin(B);
        double c1 = (1 + sinB) / (1 - sinB);
        double c2 = (1 + e * sinB) / (1 - e * sinB);
        double q = Math.log(c1) / 2 - Math.log(c2) * e / 2;
        return q;
    }

    // compute SB,B rad（计算SB，）
    public static double getSB(double B) {
        double sin2B = Math.sin(2 * B);
        double sin4B = Math.sin(4 * B);
        double sin6B = Math.sin(6 * B);
        double sin8B = Math.sin(8 * B);
        double sin10B = Math.sin(10 * B);
        double sin12B = Math.sin(12 * B);

        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double e8 = e6 * e2;
        double e10 = e8 * e2;
        double e12 = e10 * e2;

        double k0 = 1 + 3 / 4 * e2 + 45 / 64 * e4 + 175 / 256 * e6 + 11025 / 16384 * e8 + 43659 / 65536 * e10
                + 693693 / 1048576 * e12;
        double k2 = 3 / 8 * e2 + 15 / 32 * e4 + 525 / 1024 * e6 + 2205 / 4096 * e8 + 72765 / 131072 * e10
                + 297297 / 524288 * e12;
        double k4 = 15 / 256 * e4 + 105 / 1024 * e6 + 2205 / 16384 * e8 + 10395 / 65536 * e10 + 1486485 / 8388608 * e12;
        double k6 = 35 / 3072 * e6 + 105 / 4096 * e8 + 10395 / 262144 * e10 + 55055 / 1048576 * e12;
        double k8 = 315 / 131072 * e8 + 3465 / 524288 * e10 + 99099 / 8388608 * e12;
        double k10 = 693 / 1310720 * e10 + 9009 / 5242880 * e12;
        double k12 = 1001 / 8388608 * e12;

        double SB = a * (1 - e2)
                * (k0 * B - k2 * sin2B + k4 * sin4B - k6 * sin6B + k8 * sin8B - k10 * sin10B + k12 * sin12B);

        return SB;

    }

    // compute J,P,D,return D（计算轨迹结构距离D）
    public static double getStructureDistance(int trajectoryIndex1, int trajectoryIndex2) {
        double sd = -1;

        double lox1 = loxodrome.get(trajectoryIndex1);
        lox1 = Math.abs(lox1);
        double lox2 = loxodrome.get(trajectoryIndex2);
        lox2 = Math.abs(lox2);
        double azi1 = azimuth.get(trajectoryIndex1);
        double azi2 = azimuth.get(trajectoryIndex2);

        double ad = Math.abs(azi2 - azi1);
        // System.out.println("ad:"+ad);
        if (ad > Math.PI) {
            ad = 2 * Math.PI - ad;
            // System.out.println("ad2:"+ad);
        }
        // 若超过Math.PI / 2，两条轨迹方向相反，将其方向结构距离设为∞
        if (ad >= Math.PI / 2) {
           // sd = radius + 1000000;
            sd=1000000;
        } else {
            // compute J
            double J = getMin(lox1, lox2) * Math.sin(ad);// 获取轨迹方向结构距离J
            // compute P
            double P = getLocationDistance(trajectoryIndex1, trajectoryIndex2);// 获取位置结构距离P
            // 权重的大小
            sd = J * 0.4 + P * 0.6;

        }

        return sd;
    }

    // compute P:location structure distance（计算P：位置结构距离）
    public static double getLocationDistance(int trajectoryIndex1, int trajectoryIndex2) {
        double ld = 0;
        // compute P
        int firstPointIndex1 = firstPointIndex.get(trajectoryIndex1);
        int lastPointIndex1 = lastPointIndex.get(trajectoryIndex1);
        int firstPointIndex2 = firstPointIndex.get(trajectoryIndex2);
        int lastPointIndex2 = lastPointIndex.get(trajectoryIndex2);
        // exchang coordinate（变换坐标）

        double line1x1 = p[firstPointIndex1].x;
        double line1y1 = p[firstPointIndex1].y;
        double[] line1xy1 = { line1x1, line1y1 };

        double line1x2 = p[lastPointIndex1].x;
        double line1y2 = p[lastPointIndex1].y;
        double[] line1xy2 = { line1x2, line1y2 };

        double line2x1 = p[firstPointIndex2].x;
        double line2y1 = p[firstPointIndex2].y;
        double[] line2xy1 = { line2x1, line2y1 };

        double line2x2 = p[lastPointIndex2].x;
        double line2y2 = p[lastPointIndex2].y;
        double[] line2xy2 = { line2x2, line2y2 };

        // compute linear equation（计算线性方程）
        double A1 = line1y2 - line1y1;
        double B1 = line1x1 - line1x2;
        double C1 = line1x2 * line1y1 - line1x1 * line1y2;

        double A2 = line2y2 - line2y1;
        double B2 = line2x1 - line2x2;
        double C2 = line2x2 * line2y1 - line2x1 * line2y2;

        double[] minD = new double[4];
        minD[0] = getMinDistance(line1xy1, line2xy1, line2xy2, A2, B2, C2);
        minD[1] = getMinDistance(line1xy2, line2xy1, line2xy2, A2, B2, C2);
        minD[2] = getMinDistance(line2xy1, line1xy1, line1xy2, A1, B1, C1);
        minD[3] = getMinDistance(line2xy2, line1xy1, line1xy2, A1, B1, C1);

        ld = getMaxInArray(minD);
        return ld;
    }

    public static double getMinDistance(double[] line1xy, double[] line2xy1, double[] line2xy2, double A2, double B2,
                                        double C2) {
        double d = 0;
        double A1 = B2;
        double B1 = -A2;
        double C1 = -(A1 * line1xy[0] + B1 * line1xy[1]);
        // foot point（脚点）
        double[] xyv = new double[2];
        xyv[0] = (B1 * C2 - C1 * B2) / (A1 * B2 - A2 * B1);
        xyv[1] = (C1 * A2 - C2 * A1) / (A1 * B2 - A2 * B1);

        if (pointOnLine(xyv, line2xy1, line2xy2)) {
            d = getEuclideanDistance(line1xy, xyv);
        } else {
            double d1 = getEuclideanDistance(line1xy, line2xy1);
            double d2 = getEuclideanDistance(line1xy, line2xy2);
            d = getMin(d1, d2);
        }
        return d;
    }

    // compute Euclidean distance between two points（两点间的欧几里得距离）
    public static double getEuclideanDistance(double[] xy1, double[] xy2) {
        double xx = xy2[0] - xy1[0];
        double yy = xy2[1] - xy1[1];
        double ed = Math.sqrt(xx * xx + yy * yy);
        return ed;
    }

    // judge whether point is on the line or not（判断点是否在线上）
    public static boolean pointOnLine(double[] xy, double[] line2xy1, double[] line2xy2) {
        boolean result = true;
        double x = xy[0], y = xy[1];
        double xmax = 0, xmin = 0, ymax = 0, ymin = 0;
        if (line2xy1[0] > line2xy2[0]) {
            xmax = line2xy1[0];
            xmin = line2xy2[0];
        } else {
            xmax = line2xy2[0];
            xmin = line2xy1[0];
        }
        if (line2xy1[1] > line2xy2[1]) {
            ymax = line2xy1[1];
            ymin = line2xy2[1];
        } else {
            ymax = line2xy2[1];
            ymin = line2xy1[1];
        }

        if (x >= xmin && x <= xmax && y >= ymin && y <= ymax) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    // get the maximum between two double value（在两个参数之间求最大值）
    public static double getMax(double val1, double val2) {
        double max = val1;
        if (max <= val2) {
            max = val2;
        }
        return max;
    }

    // get the minimum between two double value（获取两个参数的最小值）
    public static double getMin(double val1, double val2) {
        double min = val1;
        if (min >= val2) {
            min = val2;
        }
        return min;
    }

    // get the maximum in array（在数组中获得最大值）
    public static double getMaxInArray(double[] array) {
        double max = 0;
        for (int i = 0; i < array.length; i++) {
            if (max <= array[i])
                max = array[i];
        }
        return max;
    }

    // get the minimum in array（在数组中获得最小值）
    public static double getMinInArray(double[] array) {
        double min = 0;
        for (int i = 0; i < array.length; i++) {
            if (min >= array[i])
                min = array[i];
        }
        return min;
    }

    // second to radian（二次弧度）
    public static double degree2rad(double degree) {
        double rad = degree * Math.PI / 180;
        return rad;

    }
}
