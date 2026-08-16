package com.example.unyieldingmight;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database implements Observer {

    private String DB_HOST = BuildConfig.DB_HOST;
    private String DB_PORT = BuildConfig.DB_PORT;
    private String DB_USER = BuildConfig.DB_USER;
    private String DB_PASSWORD = BuildConfig.DB_PASSWORD;
    private String DB_NAME = BuildConfig.DB_NAME;
    private String DB_URL = String.format("jdbc:mysql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    public Connection connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    @Override
    public void update() {
        // change the classes
    }

}
