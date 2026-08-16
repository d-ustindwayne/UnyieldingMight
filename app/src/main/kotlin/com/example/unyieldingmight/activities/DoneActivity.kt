package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityDoneBinding

class DoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoneBinding
    private val viewModel: DoneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.activityDoneBtnDone.setOnClickListener {
            register()
        }
    }

    private fun observeViewModel() {
        viewModel.registrationResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } else {
                binding.activityDoneBtnDone.isEnabled = true
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun register() {
        val b = intent.extras ?: return
        binding.activityDoneBtnDone.isEnabled = false

        viewModel.register(
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
            b.getString("DOB") ?: "2000-01-01",
            b.getString("Gender") ?: "OTHER"
        )
    }
}

