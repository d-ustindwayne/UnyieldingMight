package com.example.unyieldingmight;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    private static Database instance;
    private Connection connection;
    private User currentUser;
    private final String DB_HOST = BuildConfig.DB_HOST;
    private final String DB_PORT = BuildConfig.DB_PORT;
    private final String DB_USER = BuildConfig.DB_USER;
    private final String DB_PASSWORD = BuildConfig.DB_PASSWORD;
    private final String DB_NAME = BuildConfig.DB_NAME;
    private final String DB_URL = String.format("jdbc:jtds:sqlserver://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    private Database() {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static Database getInstance() {
        if (instance == null) instance = new Database();
        return instance;
    }

    public static Connection getConnection() {
        try {
            if (getInstance().connection == null || getInstance().connection.isClosed()) {
                getInstance().reconnect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getInstance().connection;
    }

    private void reconnect() {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            // Added ssl=request to handle school servers that might require basic encryption
            // Added loginTimeout to prevent the app from hanging too long
            String url = DB_URL + ";ssl=request;loginTimeout=30";
            this.connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User getCurrentUser() { return getInstance().currentUser; }

    /**
     * Authenticates a user against the UserProfile table.
     */
    public static User getUser(String email, String password) {
        String sql = "SELECT * FROM UserProfile WHERE Email = ? AND ProfilePassword = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String roleStr = rs.getString("UserClass");
                    User.Role role = (roleStr != null && roleStr.equalsIgnoreCase("ADMIN"))
                            ? User.Role.ADMIN : User.Role.CUSTOMER;

                    User loggedInUser = new User(
                            rs.getInt("ProfileId"),
                            rs.getString("FirstName") + " " + rs.getString("LastName"),
                            rs.getString("ProfilePassword"),
                            rs.getString("Email"),
                            role
                    );
                    getInstance().currentUser = loggedInUser;
                    return loggedInUser;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Registers a new customer across ProfileAddress, Customer, and UserProfile tables.
     */
    public static boolean registerCustomer(String firstName, String lastName, String email, String password,
        String street, String suburb, String city, String country, String postcode,
        float height, float weight) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert Address
            int addressId = -1;
            String addrSql = "INSERT INTO ProfileAddress (Street, Suburb, City, Country, PostCode) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(addrSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, street); pstmt.setString(2, suburb);
                pstmt.setString(3, city); pstmt.setString(4, country); pstmt.setString(5, postcode);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) { if (rs.next()) addressId = rs.getInt(1); }
            }

            // 2. Insert Customer (Set default activity multiplier)
            int customerId = -1;
            String custSql = "INSERT INTO Customer (IsMember, CustomerHeight, CustomerWeight, ActivityMultiplier, TDEE) VALUES (0, ?, ?, 1.2, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(custSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setFloat(1, height); pstmt.setFloat(2, weight);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) { if (rs.next()) customerId = rs.getInt(1); }
            }

            // 3. Insert UserProfile
            String profSql = "INSERT INTO UserProfile (Email, ProfilePassword, FirstName, LastName, Age, DateOfBirth, Gender, AddressId, CustomerId, UserClass) " +
                    "VALUES (?, ?, ?, ?, 20, '2000-01-01', 'M', ?, ?, 'CUSTOMER')";
            try (PreparedStatement pstmt = conn.prepareStatement(profSql)) {
                pstmt.setString(1, email); pstmt.setString(2, password);
                pstmt.setString(3, firstName); pstmt.setString(4, lastName);
                pstmt.setInt(5, addressId); pstmt.setInt(6, customerId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { }
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { }
        }
    }

    public static ArrayList<GymClass> getGymClassesAvailable() {
        ArrayList<GymClass> classes = new ArrayList<>();
        String sql = "SELECT * FROM GymClasses WHERE ClassStatus = 'ONGOING'";
        Connection conn = getConnection();
        if (conn == null) return classes;
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                classes.add(mapResultSetToGymClass(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return classes;
    }

    public static boolean bookClass(int classId) {
        User user = getCurrentUser();
        if (user == null) return false;
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            // SQL Server Row Locking
            String checkSql = "SELECT CurrentCapacity, MaxCapacity FROM GymClasses WITH (UPDLOCK) WHERE ClassId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, classId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt("CurrentCapacity") < rs.getInt("MaxCapacity")) {
                        // Update capacity
                        String upSql = "UPDATE GymClasses SET CurrentCapacity = CurrentCapacity + 1 WHERE ClassId = ?";
                        try (PreparedStatement upPstmt = conn.prepareStatement(upSql)) {
                            upPstmt.setInt(1, classId); upPstmt.executeUpdate();
                        }
                        // Record history
                        String histSql = "INSERT INTO ClassHistory (ProfileId, ClassId, BookingStatus) VALUES (?, ?, 'BOOKED')";
                        try (PreparedStatement hPstmt = conn.prepareStatement(histSql)) {
                            hPstmt.setInt(1, user.getId()); hPstmt.setInt(2, classId); hPstmt.executeUpdate();
                        }
                        conn.commit();
                        return true;
                    }
                }
            }
            conn.rollback();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { }
            e.printStackTrace();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { }
        }
        return false;
    }

    private static GymClass mapResultSetToGymClass(ResultSet rs) throws SQLException {
        return new GymClass.Builder()
                .ID(rs.getInt("ClassId")).name(rs.getString("ClassName"))
                .description(rs.getString("ClassDescription"))
                .currentCapacity(rs.getInt("CurrentCapacity"))
                .maxCapacity(rs.getInt("MaxCapacity"))
                .status(ClassStatus.valueOf(rs.getString("ClassStatus")))
                .avgCaloriesBurnedADay(rs.getFloat("CaloriesBurned"))
                .build();
    }
}