package com.example.unyieldingmight.Services;

import android.util.Log;

import com.example.unyieldingmight.BuildConfig;
import com.example.unyieldingmight.Models.Gender;
import com.example.unyieldingmight.Models.GymBooking;
import com.example.unyieldingmight.Models.GymClass;
import com.example.unyieldingmight.Models.Membership;
import com.example.unyieldingmight.Models.MembershipResult;
import com.example.unyieldingmight.Models.ActivityMultiplier;
import com.example.unyieldingmight.Models.Address;
import com.example.unyieldingmight.Models.ClassStatus;
import com.example.unyieldingmight.Models.Customer;
import com.example.unyieldingmight.Models.EmailVerificationData;
import com.example.unyieldingmight.Models.NewsletterType;
import com.example.unyieldingmight.Models.NewsletterSubscribers;
import com.example.unyieldingmight.Models.Profile;
import com.example.unyieldingmight.Models.Trainer;
import com.example.unyieldingmight.Utils.Security;
import com.example.unyieldingmight.Models.User;
import com.example.unyieldingmight.Utils.Conversion;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    private static Database instance;
    private Connection connection;
    private User currentUser;
    private final NewsletterSubscribers newsletterSubscribers = new NewsletterSubscribers();
    private final String DB_HOST = BuildConfig.DB_HOST;
    private final String DB_PORT = BuildConfig.DB_PORT;
    private final String DB_USER = BuildConfig.DB_USER;
    private final String DB_PASSWORD = BuildConfig.DB_PASSWORD;
    private final String DB_NAME = BuildConfig.DB_NAME;
    private final String DB_URL = String.format("jdbc:jtds:sqlserver://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    private Database() {
        reconnect();
    }

    /**
     * PreparedStatement is a precompiled query and those ? will be replaced
     * with values to be set | to prevent SQL injection
     */

    // Connection to the database is always checked for each function (if conn == null)

    // ResultSet is the result from the query executed

    // Only one instantiation
    public static Database getInstance() {
        if (instance == null) instance = new Database();
        return instance;
    }

    // Connection to database
    public static Connection getConnection() {
        try {
            if (getInstance().connection == null || getInstance().connection.isClosed()) {
                getInstance().reconnect();
            }
        } catch (SQLException e) { Log.e("DATABASE", "Connection Error: " + e.getMessage()); }
        return getInstance().connection;
    }

    private void reconnect() {
        try {
            // Connect the driver to the class
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            // URL for database connection + parameter
            String url = DB_URL + ";ssl=request;loginTimeout=30"; // Establish secure connection + timeout
            this.connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
        } catch (Exception e) { Log.e("DATABASE", "Reconnection Error: " + e.getMessage()); }
    }

    public static User getCurrentUser() { return getInstance().currentUser; }

    // To apply observer pattern
    public static NewsletterSubscribers getNewsletterSubscribers() {
        return getInstance().newsletterSubscribers;
    }

    public static Customer getCustomer(String email) {
        // Combine UserProfile, ProfileAddress, and Customer
        String sql = "SELECT up.*, c.*, pa.* FROM UserProfile up " +
                "LEFT JOIN Customer c ON up.CustomerId = c.CustomerId " +
                "LEFT JOIN ProfileAddress pa ON up.AddressId = pa.AddressId " +
                "WHERE up.Email = ?";
        // Check for valid connection
        Connection conn = getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Create address for the customer
                    Address address = new Address.Builder()
                            .street(rs.getString("Street"))
                            .city(rs.getString("City"))
                            .region(rs.getString("Region"))
                            .country(rs.getString("Country"))
                            .postcode(rs.getString("Postcode"))
                            .build();

                    // Gender condition
                    String gdStr = rs.getString("Gender");
                    Gender gd = Gender.valueOf(gdStr);

                    // Create the profile for the customer
                    Profile profile = new Profile.Builder()
                            .email(rs.getString("Email"))
                            .firstName(rs.getString("FirstName"))
                            .lastName(rs.getString("LastName"))
                            .dateOfBirth(rs.getDate("DateOfBirth"))
                            .gender(gd)
                            .age(rs.getInt("Age"))
                            .address(address)
                            .userClass(rs.getString("UserClass"))
                            .build();

                    // Activity Multiplier condition
                    float multiplierVal = rs.getFloat("ActivityMultiplier");
                    ActivityMultiplier multiplier = ActivityMultiplier.valueOf(multiplierVal);

                    // Create the Customer class
                    return new Customer.Builder()
                            .customerId(rs.getInt("CustomerId"))
                            .profile(profile)
                            .isMember(rs.getObject("MembershipId") != null)
                            .height(rs.getFloat("Height"))
                            .weight(rs.getFloat("Weight"))
                            .activityMultiplier(multiplier)
                            .tdee(rs.getFloat("TDEE"))
                            .build();
                }
            }
        } catch (Exception e) { Log.e("DATABASE", "Customer Data Fetching Failed: " + e.getMessage()); }
        return null;
    }

    public static User loginUser(String email, String password) {
        // Validates the user credential and returns the user
        User user = getUser(email, password);
        if (user != null) {
            getInstance().currentUser = user;
        }
        return user;
    }

    // Fetch the user from the database
    public static User getUser(String email, String password) {
        String sql = "SELECT * FROM UserProfile WHERE Email = ?";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("ProfilePassword");
                String inputHash = Security.hashData(password);

                // Check the hashed input and the hashed password stored in the database
                if (storedHash.equals(inputHash)) {
                    String roleStr = rs.getString("UserClass");
                    User.Role role = (roleStr.equalsIgnoreCase("ADMIN"))
                            ? User.Role.ADMIN : User.Role.CUSTOMER;

                    // Create user class for the logged user
                    return new User(
                            rs.getInt("ProfileId"),
                            rs.getString("FirstName") + " " + rs.getString("LastName"),
                            storedHash,
                            rs.getString("Email"),
                            role
                    );
                }
            }
        } catch (Exception e) { Log.e("DATABASE", "User Fetching Error: " + e.getMessage()); }
        return null;
    }

    // Create a record of the given information in the ProfileAddress, UserProfile, and Customer Table
    public static boolean registerCustomer(String firstName, String lastName, String email,
                                           String password, String street, String region, String city, String country, String postcode,
                                           float height, float weight, float multiplier, Integer membershipId, Date dob, Gender gender) {

        if (!validateEmail(email)) return false;

        Connection conn = getConnection();
        if (conn == null) return false;
        try {
            // Disables auto commit | preventing multiple record and primary key skipping increment
            conn.setAutoCommit(false);

            int addressId = -1;
            int customerId = -1;
            String addrSql = "INSERT INTO ProfileAddress (Street, City, Region, Postcode, Country) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(addrSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, street);
                pstmt.setString(2, city);
                pstmt.setString(3, region);
                pstmt.setString(4, postcode);
                pstmt.setString(5, country);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) addressId = rs.getInt(1);
            }

            // Calculate TDEE using the logic requested
            int age = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - (dob.getYear() + 1900);

            // Fix: Multiply components before passing to calculateBMR as required by Customer model
            float bmr = Customer.calculateBMR(weight * 10, height * 6.25f, age * 5, gender);

            float tdee = Conversion.rounded(bmr * multiplier);

            // Insert the Customer's stats information
            String customerSql = "INSERT INTO Customer (MembershipId, Email, Height, Weight, ActivityMultiplier, TDEE) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(customerSql, Statement.RETURN_GENERATED_KEYS)) {
                // Fix: Properly handle null MembershipId
                if (membershipId != null) pstmt.setInt(1, membershipId);
                else pstmt.setNull(1, Types.INTEGER);

                pstmt.setString(2, email);
                pstmt.setFloat(3, height); pstmt.setFloat(4, weight);
                pstmt.setFloat(5, multiplier);
                pstmt.setFloat(6, tdee);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) customerId = rs.getInt(1);
            }

            // Insert the Customer's personal information
            String profSql = "INSERT INTO UserProfile (Email, ProfilePassword, FirstName, LastName, DateOfBirth, Gender, AddressId, CustomerId, UserClass) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CUSTOMER')";
            try (PreparedStatement pstmt = conn.prepareStatement(profSql)) {
                String hashedPassword = Security.hashData(password);
                pstmt.setString(1, email);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, firstName);
                pstmt.setString(4, lastName);
                pstmt.setDate(5, dob);
                pstmt.setString(6, gender.toString());
                pstmt.setInt(7, addressId);
                pstmt.setInt(8, customerId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            Log.e("DATABASE", "Registration Failed: " + e.getMessage());
            // To return to the previous state
            try { conn.rollback(); } catch (Exception err) { Log.e("DATABASE", "Rollback Error: " + err.getMessage()); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { Log.e("DATABASE", "SetAutoCommit Error: " + e.getMessage()); }
        }
    }

    public static ArrayList<GymClass> getGymClassesAvailable() {
        // Check all gym classes that are available by "ClassStatus = 'ONGOING'"
        ArrayList<GymClass> classes = new ArrayList<>();
        String sql = "SELECT gc.*, t.FirstName as T_FirstName, t.LastName as T_LastName, t.ProfileDescription as T_Desc " +
                "FROM GymClass gc " +
                "LEFT JOIN Trainer t ON gc.TrainerId = t.TrainerId " +
                "WHERE gc.ClassStatus = 'ONGOING'";
        Connection conn = getConnection();
        if (conn == null) return classes;
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                classes.add(mapResultSetToGymClass(rs));
            }
        } catch (SQLException e) { Log.e("ERROR", e.getMessage()); };
        return classes;
    }

    public static MembershipResult validateMembership(int membershipId, String email) {
        String sql = "SELECT m.*, c.CustomerId, pa.Street, pa.City, pa.Region, pa.Postcode, pa.Country " +
                "FROM Membership m " +
                "LEFT JOIN Customer c ON m.MembershipId = c.MembershipId " +
                "LEFT JOIN ProfileAddress pa ON m.AddressId = pa.AddressId " +
                "WHERE m.MembershipId = ?";

        Connection conn = getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, membershipId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Check if already linked or email mismatch using if-else
                if (rs.getObject("CustomerId") != null) {
                    return new MembershipResult(MembershipResult.Status.ALREADY_LINKED, null);
                } else if (!rs.getString("Email").equalsIgnoreCase(email)) {
                    return new MembershipResult(MembershipResult.Status.EMAIL_MISMATCH, null);
                } else {
                    // Create membership with direct column mapping
                    Membership membership = new Membership(
                            rs.getInt("MembershipId"), rs.getString("Email"),
                            rs.getFloat("Height"), rs.getFloat("Weight"),
                            rs.getFloat("ActivityMultiplier"), rs.getFloat("TDEE"),
                            rs.getDate("DateOfBirth"), rs.getString("Gender")
                    );

                    // Add address only if the ID exists
                    if (rs.getObject("AddressId") != null) {
                        membership.setAddress(rs.getInt("AddressId"), rs.getString("Street"),
                                rs.getString("City"), rs.getString("Region"),
                                rs.getString("Postcode"), rs.getString("Country"));
                    }
                    return new MembershipResult(MembershipResult.Status.SUCCESS, membership);
                }
            } else {
                return new MembershipResult(MembershipResult.Status.NOT_FOUND, null);
            }
        } catch (SQLException e) {
            Log.e("DATABASE", "Validation Error: " + e.getMessage());
            return null;
        }
    }

    // For getting recommended gym class
//    public static ArrayList<GymClass> getRecommendedGymClasses(Customer customer) {
//        ArrayList<GymClass> allOngoing = getGymClassesAvailable();
//        ArrayList<GymClass> recommended = new ArrayList<>();
//        float userTdee = customer.getTDEE();
//
//        // Check all gym classes that burns at least 12% of user's tdee
//        for (GymClass gc : allOngoing) {
//            float classBurn = gc.getAvgCaloriesBurnedPerDay();
//            if (classBurn >= (userTdee * 0.12f)) {
//                recommended.add(gc);
//            }
//        }
//        return recommended;
//    }

//    public static boolean updateClassStatus(int classId, ClassStatus status) {
//        String sql = "UPDATE GymClass SET ClassStatus = ? WHERE ClassId = ?";
//        Connection conn = getConnection();
//        if (conn == null) return false;
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, status.name());
//            pstmt.setInt(2, classId);
//            int rows = pstmt.executeUpdate();
//            if (rows > 0) {
//                // Sends email about the class being finished or cancelled.
//                NewsletterType type = (status == ClassStatus.COMPLETE) ? NewsletterType.CLASS_FINISHED : NewsletterType.CLASS_CANCELLED;
//                NewsletterSubscribers subscribers = getNewsletterSubscribers();
//                subscribers.setLatestUpdateType(type);
//                subscribers.notifyObserver();
//                return true;
//            }
//        } catch (SQLException e) { Log.e("ERROR", e.getMessage()); };
//        return false;
//    }

//    public static ArrayList<GymBooking> getBookingHistory() {
//        // Get current user's booking history
//        ArrayList<GymBooking> history = new ArrayList<>();
//        User user = getCurrentUser();
//        if (user == null) return history;
//
//        String sql = "SELECT b.*, c.*, t.FirstName as T_FirstName, t.LastName as T_LastName, t.ProfileDescription as T_Desc " +
//                "FROM GymClassBooking b " +
//                "JOIN GymClass c ON b.ClassId = c.ClassId " +
//                "LEFT JOIN Trainer t ON c.TrainerId = t.TrainerId " +
//                "WHERE b.ProfileId = ? " +
//                "ORDER BY b.BookingDate DESC";
//
//        Connection conn = getConnection();
//        if (conn == null) return history;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, user.getId());
//            try (ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    GymClass gc = mapResultSetToGymClass(rs);
//                    history.add(new GymBooking.Builder()
//                            .historyId(rs.getInt("HistoryId"))
//                            .gymClass(gc)
//                            .status(ClassStatus.valueOf(rs.getString("BookingStatus")))
//                            .bookingDate(rs.getTimestamp("BookingDate"))
//                            .build()
//                    );
//                }
//            }
//        } catch (SQLException e) {
//            Log.e("ERROR", e.getMessage());
//        }
//        return history;
//    }

    public static boolean editClass(int classId, String name, String description, Timestamp startDateTime, Timestamp endDateTime, int maxCapacity, float avgCalorieBurnedPerDay) {
        // Edit class information
        Connection conn = getConnection();
        if (conn == null) return false;
        String sql = "UPDATE GymClass SET ClassName = ?, ClassDescription = ?, StartDateTime = ?, EndDateTime = ?, MaxCapacity = ?, AvgCaloriesBurnedPerDay = ? WHERE ClassId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setTimestamp(3, startDateTime);
            pstmt.setTimestamp(4, endDateTime);
            pstmt.setInt(5, maxCapacity);
            pstmt.setFloat(6, avgCalorieBurnedPerDay);
            pstmt.setInt(7, classId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.e("DATABASE_ERROR", "Error updating class: " + e.getMessage(), e);
            return false;
        }
    }

    public static boolean bookClass(int classId) {
        User user = getCurrentUser();
        Connection conn = getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);

            // Check if already booked
            String checkBookedSql = "SELECT 1 FROM GymClassBooking WHERE ProfileId = ? AND ClassId = ? AND BookingStatus = 'BOOKED'";
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkBookedSql)) {
                checkPstmt.setInt(1, user.getId());
                checkPstmt.setInt(2, classId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        return false; // Already booked
                    }
                }
            }

            String checkSql = "SELECT CurrentCapacity, MaxCapacity FROM GymClass WITH (UPDLOCK) WHERE ClassId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, classId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt("CurrentCapacity") < rs.getInt("MaxCapacity")) {
                        String upSql = "UPDATE GymClass SET CurrentCapacity = CurrentCapacity + 1 WHERE ClassId = ?";
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
                            getNewsletterSubscribers().notifySpecificObserver(customer, NewsletterType.BOOK_CONFIRMED);
                        }

                        return true;
                    }
                }
            }
            conn.rollback();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException err) { Log.e("SQL ERROR", err.getMessage()); }
            Log.e("ERROR", e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { Log.e("SQL ERROR", ex.getMessage()); }
        }
        return false;
    }

    public static boolean isAlreadyBooked(int classId) {
        User user = getCurrentUser();
        if (user == null) return false;
        String sql = "SELECT 1 FROM GymClassBooking WHERE ProfileId = ? AND ClassId = ? AND BookingStatus = 'BOOKED'";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Log.e("ERROR", e.getMessage());
        }
        return false;
    }

    private static GymClass mapResultSetToGymClass(ResultSet rs) throws SQLException {
        Trainer trainer = null;
        try {
            String tFirst = rs.getString("T_FirstName");
            if (tFirst != null) {
                trainer = new Trainer.Builder()
                        .firstName(tFirst)
                        .lastName(rs.getString("T_LastName"))
                        .profileDescription(rs.getString("T_Desc"))
                        .build();
            }
        } catch (SQLException e) { Log.e("SQL ERROR", e.getMessage()); }

        return new GymClass.Builder()
                .ID(rs.getInt("ClassId")).name(rs.getString("ClassName"))
                .description(rs.getString("ClassDescription"))
                .trainer(trainer)
                .startDateTime(rs.getTimestamp("StartDateTime"))
                .endDateTime(rs.getTimestamp("EndDateTime"))
                .currentCapacity(rs.getInt("CurrentCapacity"))
                .maxCapacity(rs.getInt("MaxCapacity"))
                .status(ClassStatus.valueOf(rs.getString("ClassStatus")))
                .avgCaloriesBurnedPerDay(rs.getFloat("AvgCaloriesBurnedPerDay"))
                .build();
    }

    public static boolean validateEmail(String email) {
        // Verify the email if it is
        EmailVerification ev = new EmailVerification().email(email).verify();
        EmailVerificationData evData = ev.getData();
        boolean isSafe = "true".equalsIgnoreCase(evData.safe_to_send());
        boolean isValid = "valid".equalsIgnoreCase(evData.result());
        boolean isDisposable = "true".equalsIgnoreCase(evData.disposable());

        // Check if email it is not safe, valid, or disposable
        if (!(isSafe || isValid) || isDisposable) {
            Log.w("DATABASE_ERROR", "Registration blocked by QEV for: " + email + ". Reason: " + evData.reason());
            return false;
        }
        return true;
    }
}