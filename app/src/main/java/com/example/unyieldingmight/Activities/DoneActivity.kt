package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.Models.Gender
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.Database
import java.sql.Date

class DoneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_done)
    }

    fun nextActivityHome(v: View) {
        v.isEnabled = false

        val b = intent.extras ?: run {
            v.isEnabled = true
            return
        }

        val fName = b.getString("FirstName")
        val lName = b.getString("LastName")
        val email = b.getString("Email")
        val password = b.getString("Password")
        val dob = b.getString("DOB")
        val street = b.getString("Street")
        val region = b.getString("Region")
        val city = b.getString("City")
        val country = b.getString("Country")
        val postcode = b.getString("Postcode")
        val genderStr = b.getString("Gender") ?: "OTHER"
        val membershipId = if (b.containsKey("MembershipId")) b.getInt("MembershipId") else null
        val height = b.getFloat("Height")
        val weight = b.getFloat("Weight")
        val multiplier = b.getFloat("ActivityMultiplier")

        Thread {
            val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.OTHER }
            val success = Database.registerCustomer(
                fName, lName, email, password ?: "",
                street, city, region, postcode, country,
                height, weight, multiplier, membershipId,
                Date.valueOf(dob ?: "2000-01-01"), gender
            )
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show()
                    val i = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(i)
                } else {
                    v.isEnabled = true
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
