package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupListeners()
    }

    private fun setupSpinners() {
        val adapterCountry = ArrayAdapter.createFromResource(
            this,
            com.example.unyieldingmight.R.array.country_array, android.R.layout.simple_spinner_item
        )
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityProfileSpinCountry.adapter = adapterCountry
    }

    private fun setupListeners() {
        binding.activityProfileBtnProceed.setOnClickListener {
            proceed()
        }

        binding.activityProfileBtnBack.setOnClickListener {
            finish()
        }
    }

    private fun proceed() {
        val day = binding.activityProfileEtDay.text.toString().trim()
        val month = binding.activityProfileEtMonth.text.toString().trim()
        val year = binding.activityProfileEtYear.text.toString().trim()
        val street = binding.activityProfileEtStreet.text.toString().trim()
        val city = binding.activityProfileEtCity.text.toString().trim()
        val region = binding.activityProfileEtRegion.text.toString().trim()
        val postcode = binding.activityProfileEtPostcode.text.toString().trim()

        if (binding.activityProfileSpinCountry.selectedItem == null) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show()
            return
        }
        val country = binding.activityProfileSpinCountry.selectedItem.toString()

        val fields = listOf(day, month, year, street, city, region, postcode)
        if (fields.any { it.isEmpty() }) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val DOB = String.format("%s-%s-%s", year, month, day)

        val intent = Intent(this, StatsActivity::class.java).apply {
            putExtras(this@ProfileActivity.intent.extras ?: Bundle())
            putExtra("DOB", DOB)
            putExtra("Street", street)
            putExtra("City", city)
            putExtra("Region", region)
            putExtra("Postcode", postcode)
            putExtra("Country", country)
        }
        startActivity(intent)
    }
}

