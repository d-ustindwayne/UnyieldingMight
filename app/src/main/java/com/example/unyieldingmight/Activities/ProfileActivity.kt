package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.R

class ProfileActivity : AppCompatActivity() {
    private lateinit var etDay: EditText
    private lateinit var etMonth: EditText
    private lateinit var etYear: EditText
    private lateinit var etStreet: EditText
    private lateinit var etCity: EditText
    private lateinit var etRegion: EditText
    private lateinit var etPostcode: EditText
    private lateinit var spCountry: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etDay = findViewById(R.id.activity_profile_et_day)
        etMonth = findViewById(R.id.activity_profile_et_month)
        etYear = findViewById(R.id.activity_profile_et_year)
        etStreet = findViewById(R.id.activity_profile_et_street)
        etCity = findViewById(R.id.activity_profile_et_city)
        etRegion = findViewById(R.id.activity_profile_et_region)
        etPostcode = findViewById(R.id.activity_profile_et_postcode)
        spCountry = findViewById(R.id.activity_profile_spin_country)

        val adapterCountry = ArrayAdapter.createFromResource(
            this,
            R.array.country_array, android.R.layout.simple_spinner_item
        )
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCountry.adapter = adapterCountry
    }

    fun previousActivityMembership(v: View?) {
        finish()
    }

    fun proceed(v: View?) {
        val day = etDay.text.toString().trim()
        val month = etMonth.text.toString().trim()
        val year = etYear.text.toString().trim()
        val street = etStreet.text.toString().trim()
        val city = etCity.text.toString().trim()
        val region = etRegion.text.toString().trim()
        val postcode = etPostcode.text.toString().trim()

        if (spCountry.selectedItem == null) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show()
            return
        }
        val country = spCountry.selectedItem.toString()

        val fields = listOf(day, month, year, street, city, region, postcode)
        if (fields.any { it.isEmpty() }) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Format DOB for SQL: YYYY-MM-DD
        val dob = String.format("%s-%s-%s", year, month, day)

        val i = Intent(this, StatsActivity::class.java).apply {
            putExtras(intent.extras ?: Bundle())
            putExtra("DOB", dob)
            putExtra("Street", street)
            putExtra("City", city)
            putExtra("Region", region)
            putExtra("Postcode", postcode)
            putExtra("Country", country)
        }
        startActivity(i)
    }
}
