package com.example.dbscan.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcUtils {

    private static String driverName = "org.postgresql.Driver";
    public static String url = "jdbc:postgresql://localhost:5432/AIS";
    public static String username = "postgres";
    public static String password = "postgres";

    static {
        try {
            Class.forName(driverName);
            System.out.println("建立驱动成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {

        Connection con = null;
        try {
            System.out.println("开始连接数据库");
            con = DriverManager.getConnection(url, username, password);
            System.out.println("成功连接数据库");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}