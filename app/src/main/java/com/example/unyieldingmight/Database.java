package com.example.unyieldingmight;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    private static Database instance;
    private Connection connection;

    private final String DB_HOST = BuildConfig.DB_HOST;
    private final String DB_PORT = BuildConfig.DB_PORT;
    private final String DB_USER = BuildConfig.DB_USER;
    private final String DB_PASSWORD = BuildConfig.DB_PASSWORD;
    private final String DB_NAME = BuildConfig.DB_NAME;
    private final String DB_URL = String.format("jdbc:mysql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    private Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            // For JUSTIN: add an error ui popup here
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    // Customer Management - View Customer, Add Customer, Check Login Customer, Register Customer Update Customer Information
    // Gym Class Management - View Class, Add Class, Update Class Information
    // History Management - Add to History (executed when customer book a class), View History
    // Booking Management - Add Booking (Book), View Booking, Update Status

}