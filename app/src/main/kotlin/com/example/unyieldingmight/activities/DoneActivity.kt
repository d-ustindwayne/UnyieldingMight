package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unyieldingmight.R
import com.example.unyieldingmight.databinding.ActivityDoneBinding
import com.example.unyieldingmight.models.Gender
import com.example.unyieldingmight.services.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class DoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.activityDoneBtnDone.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val b = intent.extras ?: return
        binding.activityDoneBtnDone.isEnabled = false

        lifecycleScope.launch {
            try {
                val dob = b.getString("DOB") ?: "2000-01-01"
                val genderStr = b.getString("Gender") ?: "OTHER"
                val gender = try {
                    Gender.valueOf(genderStr)
                } catch (e: Exception) {
                    Gender.OTHER
                }

                val success = withContext(Dispatchers.IO) {
                    Database.registerCustomer(
                        b.getString("FirstName"),
                        b.getString("LastName"),
                        b.getString("Email"),
                        b.getString("Password") ?: "",
                        b.getString("Street"),
                        b.getString("City"),
                        b.getString("Region"),
                        b.getString("Postcode"),
                        b.getString("Country"),
                        b.getFloat("Height"),
                        b.getFloat("Weight"),
                        b.getFloat("ActivityMultiplier"),
                        if (b.containsKey("MembershipId")) b.getInt("MembershipId") else null,
                        Date.valueOf(dob),
                        gender
                    )
                }

                if (success) {
                    ModalActivity.start(
                        context = this@DoneActivity,
                        title = getString(R.string.registration_complete),
                        message = getString(R.string.membership_linked_profile_set),
                        buttonText = getString(R.string.btn_get_started),
                        targetActivity = LoginActivity::class.java
                    )
                    finish()
                } else {
                    binding.activityDoneBtnDone.isEnabled = true
                    Toast.makeText(this@DoneActivity, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DoneActivity", "Registration error: ${e.message}")
                binding.activityDoneBtnDone.isEnabled = true
                Toast.makeText(this@DoneActivity, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
