package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.Models.MembershipResult
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.Database
import java.text.SimpleDateFormat
import java.util.Locale

class LinkmembershipActivity : AppCompatActivity() {
    private lateinit var etMembershipId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linkmembership)

        etMembershipId = findViewById(R.id.activity_linkmembership_et_membershipId)
    }

    fun nextActivityActivityLevel(v: View?) {
        val idStr = etMembershipId.text.toString().trim()
        if (idStr.isEmpty()) {
            Toast.makeText(this, "Please enter your Membership ID", Toast.LENGTH_SHORT).show()
            return
        }

        val membershipId: Int
        try {
            membershipId = idStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid ID format", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            val email = intent.getStringExtra("Email")
            val result: MembershipResult = Database.validateMembership(membershipId, email)
            runOnUiThread {
                when (result.status) {
                    MembershipResult.Status.SUCCESS -> {
                        val membership = result.membership ?: return@runOnUiThread
                        Toast.makeText(this, "Membership linked successfully!", Toast.LENGTH_SHORT).show()
                        val i = Intent(this, DoneActivity::class.java).apply {
                            putExtras(intent.extras ?: Bundle())
                            putExtra("MembershipId", membershipId)

                            val dobStr = membership.dOB?.let {
                                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it)
                            } ?: "2000-01-01"

                            putExtra("DOB", dobStr)
                            putExtra("Gender", membership.gender ?: "MALE")
                            putExtra("Height", membership.height)
                            putExtra("Weight", membership.weight)
                            putExtra("ActivityMultiplier", membership.activityMultiplier)
                            putExtra("TDEE", membership.tdee)

                            if (membership.addressId != null) {
                                putExtra("Street", membership.street)
                                putExtra("City", membership.city)
                                putExtra("Region", membership.region)
                                putExtra("Postcode", membership.postcode)
                                putExtra("Country", membership.country)
                            }
                        }
                        startActivity(i)
                    }

                    MembershipResult.Status.NOT_FOUND -> Toast.makeText(
                        this,
                        "Membership ID not found.",
                        Toast.LENGTH_LONG
                    ).show()

                    MembershipResult.Status.EMAIL_MISMATCH -> Toast.makeText(
                        this,
                        "Email on record does not match your input.",
                        Toast.LENGTH_LONG
                    ).show()

                    MembershipResult.Status.ALREADY_LINKED -> Toast.makeText(
                        this,
                        "This membership is already linked to another account.",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    null -> Toast.makeText(this, "A connection error occurred.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun previousActivityMembership(v: View?) {
        finish()
    }
}
