package com.example.dbscan.MyJava;

/**
 * Created by Administrator on 2020/5/8.
 */
public class Trajectory {

    int index;									// index of the Trajectory
    public boolean isCorepoint=false;
    public String cluster_id=""+"-1";
    public double eps_value = 0;				// Trajectory is clustered using the eps_value epsilon value
    public int cluster = 0;
    public String pid="";
}
