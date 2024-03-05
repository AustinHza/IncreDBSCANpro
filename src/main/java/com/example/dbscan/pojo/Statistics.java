package com.example.dbscan.pojo;

public class Statistics {
    private String time;
    private int counts;

    @Override
    public String toString() {
        return "Statistics{" +
                "time='" + time + '\'' +
                ", counts=" + counts +
                '}';
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    public Statistics(String time, int counts) {
        this.time = time;
        this.counts = counts;
    }

    public Statistics() {
    }
}
