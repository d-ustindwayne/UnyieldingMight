package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.models.MembershipResult
import com.example.unyieldingmight.databinding.ActivityLinkmembershipBinding
import java.text.SimpleDateFormat
import java.util.Locale

class LinkmembershipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLinkmembershipBinding
    private val viewModel: LinkMembershipViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkmembershipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.activityLinkmembershipBtnLink.setOnClickListener {
            val idStr = binding.activityLinkmembershipEtMembershipId.text.toString().trim()
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Please enter your Membership ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                viewModel.validate(idStr.toInt(), intent.getStringExtra("Email"))
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid ID format: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.activityLinkmembershipBtnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.validationResult.observe(this) { result ->
            if (result == null) {
                Toast.makeText(this, "A connection error occurred.", Toast.LENGTH_SHORT).show()
                return@observe
            }

            when (result.status) {
                MembershipResult.Status.SUCCESS -> {
                    val membership = result.membership ?: return@observe
                    Toast.makeText(this, "Membership linked successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DoneActivity::class.java).apply {
                        putExtras(this@LinkmembershipActivity.intent.extras ?: Bundle())
                        putExtra("MembershipId", membership.ID)
                        putExtra("DOB", membership.DOB?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) } ?: "2000-01-01")
                        putExtra("Gender", membership.gender ?: "MALE")
                        putExtra("Height", membership.height)
                        putExtra("Weight", membership.weight)
                        putExtra("ActivityMultiplier", membership.activityMultiplier)
                        putExtra("TDEE", membership.TDEE)

                        if (membership.addressID != null) {
                            putExtra("Street", membership.street)
                            putExtra("City", membership.city)
                            putExtra("Region", membership.region)
                            putExtra("Postcode", membership.postcode)
                            putExtra("Country", membership.country)
                        }
                    }
                    startActivity(intent)
                }
                MembershipResult.Status.NOT_FOUND -> Toast.makeText(this, "Membership ID not found.", Toast.LENGTH_LONG).show()
                MembershipResult.Status.EMAIL_MISMATCH -> Toast.makeText(this, "Email on record does not match your input.", Toast.LENGTH_LONG).show()
                MembershipResult.Status.ALREADY_LINKED -> Toast.makeText(this, "This membership is already linked to another account.", Toast.LENGTH_LONG).show()
                else -> Toast.makeText(this, "An unknown error occurred.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

