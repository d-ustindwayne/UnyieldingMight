package com.example.unyieldingmight.Services

import android.util.Log
import com.example.unyieldingmight.BuildConfig
import com.example.unyieldingmight.Models.*
import com.example.unyieldingmight.Utils.Conversion
import com.example.unyieldingmight.Utils.Security
import java.sql.*
import java.util.Calendar

class Database private constructor() {
    private var connection: Connection? = null
    private var currentUser: User? = null
    private val newsletterSubscribers: NewsletterSubscribers = NewsletterSubscribers()
    private val DB_HOST: String = BuildConfig.DB_HOST
    private val DB_PORT: String? = BuildConfig.DB_PORT
    private val DB_USER: String? = BuildConfig.DB_USER
    private val DB_PASSWORD: String? = BuildConfig.DB_PASSWORD
    private val DB_NAME: String? = BuildConfig.DB_NAME
    private val DB_URL = String.format("jdbc:jtds:sqlserver://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME)

    init {
        reconnect()
    }

    private fun reconnect() {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val url = DB_URL + ";ssl=request;loginTimeout=30"
            this.connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD)
        } catch (e: Exception) {
            Log.e("DATABASE", "Reconnection Error: " + e.message)
        }
    }

    companion object {
        var instance: Database? = null
            get() {
                if (field == null) field = Database()
                return field
            }
            private set

        fun getConnection(): Connection? {
            try {
                if (instance!!.connection == null || instance!!.connection!!.isClosed) {
                    instance!!.reconnect()
                }
            } catch (e: SQLException) {
                Log.e("DATABASE", "Connection Error: " + e.message)
            }
            return instance!!.connection
        }

        @JvmStatic
        fun getCurrentUser(): User? {
            return instance!!.currentUser
        }

        fun getNewsletterSubscribers(): NewsletterSubscribers {
            return instance!!.newsletterSubscribers
        }

        @JvmStatic
        fun getCustomer(email: String?): Customer? {
            val sql = "SELECT up.*, c.*, pa.* FROM UserProfile up " +
                    "LEFT JOIN Customer c ON up.CustomerId = c.CustomerId " +
                    "LEFT JOIN ProfileAddress pa ON up.AddressId = pa.AddressId " +
                    "WHERE up.Email = ?"
            val conn: Connection = getConnection() ?: return null

            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, email)
                    pstmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            val address = Address(
                                street = rs.getString("Street"),
                                city = rs.getString("City"),
                                region = rs.getString("Region"),
                                country = rs.getString("Country"),
                                postcode = rs.getString("Postcode")
                            )

                            val gdStr = rs.getString("Gender")
                            val gd = try { Gender.valueOf(gdStr) } catch (e: Exception) { Gender.OTHER }

                            val profile = Profile(
                                email = rs.getString("Email"),
                                firstName = rs.getString("FirstName"),
                                lastName = rs.getString("LastName"),
                                dateOfBirth = rs.getDate("DateOfBirth"),
                                gender = gd,
                                age = rs.getInt("Age"),
                                address = address,
                                userClass = rs.getString("UserClass")
                            )

                            val multiplierVal = rs.getFloat("ActivityMultiplier")
                            val multiplier = ActivityMultiplier.valueOf(multiplierVal)

                            return Customer(
                                customerId = rs.getInt("CustomerId"),
                                profile = profile,
                                isMember = rs.getObject("MembershipId") != null,
                                height = rs.getFloat("Height"),
                                weight = rs.getFloat("Weight"),
                                activityMultiplier = multiplier.multiplier,
                                tdee = rs.getFloat("TDEE")
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DATABASE", "Customer Data Fetching Failed: " + e.message)
            }
            return null
        }

        @JvmStatic
        fun loginUser(email: String?, passwordHash: String): User? {
            val user = getUser(email, passwordHash)
            if (user != null) {
                instance!!.currentUser = user
            }
            return user
        }

        @JvmStatic
        fun getUser(email: String?, passwordHash: String): User? {
            val sql = "SELECT * FROM UserProfile WHERE Email = ? AND Password = ?"
            val conn = getConnection() ?: return null

            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, email)
                    pstmt.setString(2, passwordHash)
                    pstmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            val roleStr = rs.getString("UserClass")
                            val role = if ("ADMIN".equals(roleStr, ignoreCase = true)) User.Role.ADMIN else User.Role.CUSTOMER
                            return User(
                                id = rs.getInt("UserId"),
                                username = rs.getString("Email"),
                                password = rs.getString("Password"),
                                email = rs.getString("Email"),
                                userClass = role
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DATABASE", "User Fetching Failed: " + e.message)
            }
            return null
        }

        @JvmStatic
        fun registerCustomer(
            firstName: String?, lastName: String?, email: String?, passwordHash: String,
            street: String?, city: String?, region: String?, postcode: String?, country: String?,
            height: Float, weight: Float, activityMultiplier: Float, addressId: Int?,
            dateOfBirth: Date, gender: Gender
        ): Boolean {
            val conn = getConnection() ?: return false
            try {
                conn.autoCommit = false
                
                var actualAddressId = addressId
                if (actualAddressId == null) {
                    val addressSql = "INSERT INTO ProfileAddress (Street, City, Region, Postcode, Country) VALUES (?, ?, ?, ?, ?)"
                    conn.prepareStatement(addressSql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                        pstmt.setString(1, street)
                        pstmt.setString(2, city)
                        pstmt.setString(3, region)
                        pstmt.setString(4, postcode)
                        pstmt.setString(5, country)
                        pstmt.executeUpdate()
                        pstmt.generatedKeys.use { rs ->
                            if (rs.next()) actualAddressId = rs.getInt(1)
                        }
                    }
                }

                val customerSql = "INSERT INTO Customer (Height, Weight, ActivityMultiplier) VALUES (?, ?, ?)"
                var customerId = 0
                conn.prepareStatement(customerSql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                    pstmt.setFloat(1, height)
                    pstmt.setFloat(2, weight)
                    pstmt.setFloat(3, activityMultiplier)
                    pstmt.executeUpdate()
                    pstmt.generatedKeys.use { rs ->
                        if (rs.next()) customerId = rs.getInt(1)
                    }
                }

                val userProfileSql = "INSERT INTO UserProfile (FirstName, LastName, Email, Password, Age, AddressId, DateOfBirth, Gender, CustomerId, UserClass) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                conn.prepareStatement(userProfileSql).use { pstmt ->
                    pstmt.setString(1, firstName)
                    pstmt.setString(2, lastName)
                    pstmt.setString(3, email)
                    pstmt.setString(4, passwordHash)
                    pstmt.setInt(5, 0) // Age should probably be calculated
                    pstmt.setInt(6, actualAddressId!!)
                    pstmt.setDate(7, dateOfBirth)
                    pstmt.setString(8, gender.name)
                    pstmt.setInt(9, customerId)
                    pstmt.setString(10, "CUSTOMER")
                    pstmt.executeUpdate()
                }

                conn.commit()
                return true
            } catch (e: Exception) {
                Log.e("DATABASE", "Registration Failed: " + e.message)
                try { conn.rollback() } catch (ex: SQLException) {}
            } finally {
                try { conn.autoCommit = true } catch (ex: SQLException) {}
            }
            return false
        }

        val gymClassesAvailable: ArrayList<GymClass>
            get() {
                val classes = ArrayList<GymClass>()
                val sql = "SELECT gc.*, t.FirstName, t.LastName, t.ProfileDescription FROM GymClass gc JOIN Trainer t ON gc.TrainerID = t.TrainerID"
                val conn = getConnection() ?: return classes

                try {
                    conn.createStatement().use { stmt ->
                        stmt.executeQuery(sql).use { rs ->
                            while (rs.next()) {
                                classes.add(mapResultSetToGymClass(rs))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DATABASE", "Gym Classes Fetching Failed: " + e.message)
                }
                return classes
            }

        @JvmStatic
        fun validateMembership(membershipId: Int, email: String?): MembershipResult {
            val sql = "SELECT * FROM Membership WHERE MembershipId = ?"
            val conn = getConnection() ?: return MembershipResult(MembershipResult.Status.NOT_FOUND, null)

            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, membershipId)
                    pstmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            val dbEmail = rs.getString("Email")
                            if (dbEmail != email) return MembershipResult(MembershipResult.Status.EMAIL_MISMATCH, null)

                            val membership = Membership(
                                id = rs.getInt("MembershipId"),
                                email = dbEmail,
                                height = rs.getFloat("Height"),
                                weight = rs.getFloat("Weight"),
                                activityMultiplier = rs.getFloat("ActivityMultiplier"),
                                tdee = rs.getFloat("TDEE"),
                                dOB = rs.getDate("DateOfBirth"),
                                gender = rs.getString("Gender")
                            )
                            
                            // Check if already linked
                            val linkCheck = "SELECT * FROM Customer WHERE MembershipId = ?"
                            conn.prepareStatement(linkCheck).use { lpstmt ->
                                lpstmt.setInt(1, membershipId)
                                lpstmt.executeQuery().use { lrs ->
                                    if (lrs.next()) return MembershipResult(MembershipResult.Status.ALREADY_LINKED, membership)
                                }
                            }

                            return MembershipResult(MembershipResult.Status.SUCCESS, membership)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DATABASE", "Membership Validation Failed: " + e.message)
            }
            return MembershipResult(MembershipResult.Status.NOT_FOUND, null)
        }

        @JvmStatic
        fun editClass(id: Int, title: String?, description: String?, start: Timestamp?, end: Timestamp?, maxCap: Int, avgCal: Float): Boolean {
            val sql = "UPDATE GymClass SET ClassName = ?, Description = ?, StartDateTime = ?, EndDateTime = ?, MaxCapacity = ?, AvgCaloriesBurnedPerDay = ? WHERE ClassId = ?"
            val conn = getConnection() ?: return false
            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, title)
                    pstmt.setString(2, description)
                    pstmt.setTimestamp(3, start)
                    pstmt.setTimestamp(4, end)
                    pstmt.setInt(5, maxCap)
                    pstmt.setFloat(6, avgCal)
                    pstmt.setInt(7, id)
                    return pstmt.executeUpdate() > 0
                }
            } catch (e: Exception) {
                Log.e("DATABASE", "Class Edit Failed: " + e.message)
            }
            return false
        }

        @JvmStatic
        fun bookClass(classId: Int): Boolean {
            val conn = getConnection() ?: return false
            try {
                conn.autoCommit = false
                val updateSql = "UPDATE GymClass SET CurrentCapacity = CurrentCapacity + 1 WHERE ClassId = ? AND CurrentCapacity < MaxCapacity"
                conn.prepareStatement(updateSql).use { pstmt ->
                    pstmt.setInt(1, classId)
                    if (pstmt.executeUpdate() > 0) {
                        val historySql = "INSERT INTO GymBooking (CustomerId, ClassId, BookingDate, Status) VALUES (?, ?, ?, ?)"
                        conn.prepareStatement(historySql).use { hpstmt ->
                            hpstmt.setInt(1, (instance!!.currentUser as? User)?.id ?: 0) // This is a bit loose but following original logic
                            hpstmt.setInt(2, classId)
                            hpstmt.setTimestamp(3, Timestamp(System.currentTimeMillis()))
                            hpstmt.setString(4, ClassStatus.ONGOING.name)
                            hpstmt.executeUpdate()
                        }
                        conn.commit()
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.e("DATABASE", "Booking Failed: " + e.message)
                try { conn.rollback() } catch (ex: SQLException) {}
            } finally {
                try { conn.autoCommit = true } catch (ex: SQLException) {}
            }
            return false
        }

        @JvmStatic
        fun isAlreadyBooked(classId: Int): Boolean {
            val sql = "SELECT * FROM GymBooking WHERE CustomerId = ? AND ClassId = ?"
            val conn = getConnection() ?: return false
            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, (instance!!.currentUser as? User)?.id ?: 0)
                    pstmt.setInt(2, classId)
                    pstmt.executeQuery().use { rs ->
                        return rs.next()
                    }
                }
            } catch (e: Exception) {
                Log.e("DATABASE", "Booking Check Failed: " + e.message)
            }
            return false
        }

        private fun mapResultSetToGymClass(rs: ResultSet): GymClass {
            val trainer = Trainer(
                trainerID = rs.getInt("TrainerID"),
                firstName = rs.getString("FirstName"),
                lastName = rs.getString("LastName"),
                profileDescription = rs.getString("ProfileDescription")
            )
            
            return GymClass(
                iD = rs.getInt("ClassId"),
                name = rs.getString("ClassName"),
                trainer = trainer,
                startDateTime = rs.getDate("StartDateTime"),
                endDateTime = rs.getDate("EndDateTime"),
                description = rs.getString("Description"),
                currentCapacity = rs.getInt("CurrentCapacity"),
                maxCapacity = rs.getInt("MaxCapacity"),
                status = try { ClassStatus.valueOf(rs.getString("Status")) } catch (e: Exception) { ClassStatus.ONGOING },
                avgCaloriesBurnedPerDay = rs.getFloat("AvgCaloriesBurnedPerDay")
            )
        }

        @JvmStatic
        fun validateEmail(email: String?): Boolean {
            val sql = "SELECT * FROM UserProfile WHERE Email = ?"
            val conn = getConnection() ?: return false
            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, email)
                    pstmt.executeQuery().use { rs ->
                        return rs.next()
                    }
                }
            } catch (e: Exception) {
                Log.e("DATABASE", "Email Validation Failed: " + e.message)
            }
            return false
        }
    }
}
