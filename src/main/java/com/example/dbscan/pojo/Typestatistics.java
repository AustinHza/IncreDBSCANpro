package com.example.dbscan.pojo;

public class Typestatistics {
    private String type;
    private int counts;


    public Typestatistics() {
    }

    public Typestatistics(String type, int counts) {
        this.type = type;
        this.counts = counts;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    @Override
    public String toString() {
        return "Typestatistics{" +
                "type='" + type + '\'' +
                ", counts=" + counts +
                '}';
    }
}
