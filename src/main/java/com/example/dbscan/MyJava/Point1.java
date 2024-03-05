package com.example.dbscan.MyJava;

/**
 * Created by Administrator on 2020/5/7.
 */
public class Point1
{

    //double[] attrib_value;						// value of the attributes for the points
    double longitude;
    double latitude;
    double x;
    double y;
    int mmsid;
    int index;									// index of the point
    public boolean isCorepoint=false;
    public boolean isCommon_point=false;		// identify the points which are common between the partitions used for merging
    public String partition_id=null;					// identify the partition id of the point assigned when the partition is created
    public String cluster_id=""+0;
    public double eps_value = 0;				// Point is clustered using the eps_value epsilon value
    public int cluster = 0;
    public int flag = 0;
    public double boundary_kdist = 0;				// Kth nearest point distance for boundary points
    public Point1(String arr)
    {

        String []Linearr;
        Linearr = arr.split(",");
       // System.out.println(arr);
        //this.index=Integer.valueOf(Linearr[0]);
        this.latitude=Double.valueOf(Linearr[4].replace("[",""));
        this.longitude=Double.valueOf(Linearr[3].replace("]",""));
        this.mmsid= Integer.valueOf(Linearr[5].replace(" ",""));
        //this.partition_id = "P"+Linearr[5];
       // System.out.println(this.latitude);
        double[] xy = coordinate1.lonlat2xy(this.latitude, this.longitude);
        this.x=xy[0];
        this.y=xy[1];
       // System.out.println(this.x+" "+this.y);

    }



}
