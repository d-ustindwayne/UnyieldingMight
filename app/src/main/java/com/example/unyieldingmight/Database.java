package com.example.unyieldingmight;

import android.util.Log;

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
        reconnect();
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
            String url = DB_URL + ";ssl=request;loginTimeout=30";
            this.connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User getCurrentUser() { return getInstance().currentUser; }

    public static Customer getCustomer(String email) {
        String sql = "SELECT up.*, c.*, pa.* FROM UserProfile up " +
                "LEFT JOIN Customer c ON up.CustomerId = c.CustomerId " +
                "LEFT JOIN ProfileAddress pa ON up.AddressId = pa.AddressId " +
                "WHERE up.Email = ?";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Address address = new Address.Builder()
                            .street(rs.getString("Street"))
                            .city(rs.getString("City"))
                            .region(rs.getString("Region"))
                            .country(rs.getString("Country"))
                            .postcode(rs.getString("Postcode"))
                            .build();

                    String gdStr = rs.getString("Gender");
                    Gender gd = gdStr.equalsIgnoreCase("M") ? Gender.MALE : Gender.FEMALE;

                    Profile profile = new Profile.Builder()
                            .email(rs.getString("Email"))
                            .firstName(rs.getString("FirstName"))
                            .lastName(rs.getString("LastName"))
                            .dateOfBirth(rs.getDate("DateOfBirth"))
                            .gender(gd)
                            .age(rs.getInt("Age"))
                            .address(address)
                            .build();

                    float multiplierVal = rs.getFloat("ActivityMultiplier");
                    ActivityMultiplier multiplier;
                    if (multiplierVal == 1.2f) multiplier = ActivityMultiplier.INACTIVE;
                    else if (multiplierVal == 1.375f) multiplier = ActivityMultiplier.LIGHT;
                    else if (multiplierVal == 1.725f) multiplier = ActivityMultiplier.HEAVY;
                    else if (multiplierVal == 1.9f) multiplier = ActivityMultiplier.EXTREME;
                    else multiplier = ActivityMultiplier.MODERATE;

                    return new Customer.Builder()
                            .customerId(rs.getInt("CustomerId"))
                            .profile(profile)
                            .isMember(rs.getBoolean("IsMember"))
                            .height(rs.getFloat("CustomerHeight"))
                            .weight(rs.getFloat("CustomerWeight"))
                            .activityMultiplier(multiplier)
                            .tdee(rs.getFloat("TDEE"))
                            .build();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static User loginUser(String email, String password) {
        User user = getUser(email, password);
        if (user != null) {
            getInstance().currentUser = user;
        }
        return user;
    }

    public static User getUser(String email, String password) {
        String sql = "SELECT * FROM UserProfile WHERE Email = ?";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("ProfilePassword");
                    String inputHash;
                    try {
                        inputHash = Security.hashData(password);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }

                    if (Security.validated(storedHash, inputHash)) {
                        String roleStr = rs.getString("UserClass");
                        User.Role role = (roleStr != null && roleStr.equalsIgnoreCase("ADMIN"))
                                ? User.Role.ADMIN : User.Role.CUSTOMER;

                        return new User(
                                rs.getInt("ProfileId"),
                                rs.getString("FirstName") + " " + rs.getString("LastName"),
                                storedHash,
                                rs.getString("Email"),
                                role
                        );
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean registerCustomer(String firstName, String lastName, String email, String password,
  String street, String region, String city, String country, String postcode, float height, float weight, float multiplier, Integer membershipId, Date dob, String gender) {
        
        EmailVerification ev = new EmailVerification().email(email).verify();
        EmailVerificationData evData = ev.getData();
        if (evData != null && evData.safe_to_send() != null && evData.safe_to_send().equalsIgnoreCase("false")) {
        android.util.Log.w("DATABASE_ERROR", "Registration blocked by QEV for: " + email + ". Factor safe_to_send is false.");
            return false;
        }

        Connection conn = getConnection();
        if (conn == null) {
            android.util.Log.e("DATABASE_ERROR", "Registration failed: No connection to database.");
            return false;
        }
        try {
            conn.setAutoCommit(false);

            int addressId = -1;
            String addrSql = "INSERT INTO ProfileAddress (Street, City, Region, Postcode, Country) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(addrSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, street); 
                pstmt.setString(2, city);
                pstmt.setString(3, region); 
                pstmt.setString(4, postcode);
                pstmt.setString(5, country); 
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) { if (rs.next()) addressId = rs.getInt(1); }
            }

            int customerId = -1;
            
            // Calculate TDEE using the logic requested
            int age = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - (dob.getYear() + 1900);
            float weightComponent = 10 * weight;
            float heightComponent = 6.25f * height;
            float ageComponent = 5 * age;
            float bmr;

            // Applying user's requested logic for BMR
            if ("MALE".equalsIgnoreCase(gender) || "M".equalsIgnoreCase(gender)) {
                bmr = weightComponent + heightComponent - ageComponent + 5;
            } else if ("FEMALE".equalsIgnoreCase(gender) || "F".equalsIgnoreCase(gender)) {
                bmr = weightComponent + heightComponent - ageComponent - 161;
            } else {
                float male = weightComponent + heightComponent - ageComponent + 5;
                float female = weightComponent + heightComponent - ageComponent - 161;
                bmr = (male + female) / 2;
            }
            float tdee = bmr * multiplier;

            String custSql = "INSERT INTO Customer (MembershipId, Email, Height, Weight, ActivityMultiplier, TDEE) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(custSql, Statement.RETURN_GENERATED_KEYS)) {
                if (membershipId != null) pstmt.setInt(1, membershipId); else pstmt.setNull(1, Types.INTEGER);
                pstmt.setString(2, email);
                pstmt.setFloat(3, height); pstmt.setFloat(4, weight);
                pstmt.setFloat(5, multiplier);
                pstmt.setFloat(6, tdee);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) { if (rs.next()) customerId = rs.getInt(1); }
            }

            String profSql = "INSERT INTO UserProfile (Email, ProfilePassword, FirstName, LastName, DateOfBirth, Gender, AddressId, CustomerId, UserClass) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CUSTOMER')";
            try (PreparedStatement pstmt = conn.prepareStatement(profSql)) {
                String hashedPassword;
                try {
                    hashedPassword = Security.hashData(password);
                } catch (Exception e) {
                    e.printStackTrace();
                    conn.rollback();
                    return false;
                }
                pstmt.setString(1, email);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, firstName);
                pstmt.setString(4, lastName);
                pstmt.setDate(5, dob);
                pstmt.setString(6, gender);
                pstmt.setInt(7, addressId);
                pstmt.setInt(8, customerId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            android.util.Log.e("DATABASE_ERROR", "Registration failed: " + e.getMessage());
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

    public static MembershipResult validateMembership(int membershipId, String email) {
        String sql = "SELECT m.*, c.CustomerId FROM Membership m " +
                "LEFT JOIN Customer c ON m.MembershipId = c.MembershipId " +
                "WHERE m.MembershipId = ?";

        Connection conn = getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, membershipId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getObject("CustomerId") != null) {
                        return new MembershipResult(MembershipResult.Status.ALREADY_LINKED, null);
                    }

                    String dbEmail = rs.getString("Email");

                    if (!dbEmail.equalsIgnoreCase(email)) {
                        return new MembershipResult(MembershipResult.Status.EMAIL_MISMATCH, null);
                    }

                    Membership membership = new Membership(
                            rs.getInt("MembershipId"),
                            dbEmail,
                            rs.getFloat("Height"),
                            rs.getFloat("Weight"),
                            rs.getFloat("ActivityMultiplier"),
                            rs.getFloat("TDEE")
                    );
                    Log.d(membership.toString(), "Membership Created");
                    return new MembershipResult(MembershipResult.Status.SUCCESS, membership);
                } else {
                    return new MembershipResult(MembershipResult.Status.NOT_FOUND, null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<GymClass> getRecommendedGymClasses(Customer customer) {
        ArrayList<GymClass> allOngoing = getGymClassesAvailable();
        ArrayList<GymClass> recommended = new ArrayList<>();
        float userTdee = customer.getTDEE();

        for (GymClass gc : allOngoing) {
            float classBurn = gc.getAvgCaloriesBurnedPerDay();
            if (classBurn >= (userTdee * 0.15f)) {
                recommended.add(gc);
            }
        }
        return recommended;
    }

    public static boolean updateClassStatus(int classId, ClassStatus status) {
        String sql = "UPDATE GymClasses SET ClassStatus = ? WHERE ClassId = ?";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, classId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                NewsletterType type = (status == ClassStatus.COMPLETE) ? NewsletterType.CLASS_FINISHED : NewsletterType.CLASS_CANCELLED;
                NewsletterSubscribers.getInstance().setLatestUpdateType(type);
                NewsletterSubscribers.getInstance().notifyObserver();
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static ArrayList<GymBooking> getBookingHistory() {
        ArrayList<GymBooking> history = new ArrayList<>();
        User user = getCurrentUser();
        if (user == null) return history;

        String sql = "SELECT b.*, c.* FROM GymClassBooking b " +
                "JOIN GymClasses c ON b.ClassId = c.ClassId " +
                "WHERE b.ProfileId = ? " +
                "ORDER BY b.BookingDate DESC";

        Connection conn = getConnection();
        if (conn == null) return history;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GymClass gc = mapResultSetToGymClass(rs);
                    history.add(new GymBooking.Builder()
                            .historyId(rs.getInt("HistoryId"))
                            .gymClass(gc)
                            .status(ClassStatus.valueOf(rs.getString("BookingStatus")))
                            .bookingDate(rs.getTimestamp("BookingDate"))
                            .build()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static boolean bookClass(int classId) {
        User user = getCurrentUser();
        if (user == null) return false;
        Connection conn = getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);
            String checkSql = "SELECT CurrentCapacity, MaxCapacity FROM GymClasses WITH (UPDLOCK) WHERE ClassId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, classId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt("CurrentCapacity") < rs.getInt("MaxCapacity")) {
                        String upSql = "UPDATE GymClasses SET CurrentCapacity = CurrentCapacity + 1 WHERE ClassId = ?";
                        try (PreparedStatement upPstmt = conn.prepareStatement(upSql)) {
                            upPstmt.setInt(1, classId); upPstmt.executeUpdate();
                        }
                        String histSql = "INSERT INTO GymClassBooking (ProfileId, ClassId, BookingStatus) VALUES (?, ?, 'BOOKED')";
                        try (PreparedStatement hPstmt = conn.prepareStatement(histSql)) {
                            hPstmt.setInt(1, user.getId()); hPstmt.setInt(2, classId); hPstmt.executeUpdate();
                        }
                        
                        conn.commit();

                        Customer customer = getCustomer(user.getEmail());
                        if (customer != null) {
                            NewsletterSubscribers.getInstance().notifySpecificObserver(customer, NewsletterType.BOOK_CONFIRMED);
                        }

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
                .startDateTime(rs.getTimestamp("StartDateTime"))
                .endDateTime(rs.getTimestamp("EndDateTime"))
                .currentCapacity(rs.getInt("CurrentCapacity"))
                .maxCapacity(rs.getInt("MaxCapacity"))
                .status(ClassStatus.valueOf(rs.getString("ClassStatus")))
                .avgCaloriesBurnedPerDay(rs.getFloat("AvgCaloriesBurnedPerDay"))
                .build();
    }
}
