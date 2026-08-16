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
import com.example.unyieldingmight.Utils.Conversion
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var spinHeightUnit: Spinner
    private lateinit var spinWeightUnit: Spinner
    private lateinit var spinGender: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        etHeight = findViewById(R.id.activity_stats_et_height)
        etWeight = findViewById(R.id.activity_stats_et_weight)
        spinHeightUnit = findViewById(R.id.activity_stats_spin_height)
        spinWeightUnit = findViewById(R.id.activity_stats_spin_weight)
        spinGender = findViewById(R.id.activity_stats_spin_gender)

        val adapterHeight = ArrayAdapter.createFromResource(
            this,
            R.array.height_array, android.R.layout.simple_spinner_item
        )
        adapterHeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinHeightUnit.adapter = adapterHeight

        val adapterWeight = ArrayAdapter.createFromResource(
            this,
            R.array.weight_array, android.R.layout.simple_spinner_item
        )
        adapterWeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinWeightUnit.adapter = adapterWeight

        val adapterGender = ArrayAdapter.createFromResource(
            this,
            R.array.gender_array, android.R.layout.simple_spinner_item
        )
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinGender.adapter = adapterGender
    }

    fun previousActivityProfile(v: View?) {
        finish()
    }

    fun proceedToActivityLevel(v: View?) {
        val genderStr = spinGender.selectedItem.toString().uppercase(Locale.getDefault())
        val heightStr = etHeight.text.toString().trim()
        val weightStr = etWeight.text.toString().trim()

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter your height and weight", Toast.LENGTH_SHORT).show()
            return
        }

        var height = heightStr.toFloat()
        var weight = weightStr.toFloat()

        val hUnit = spinHeightUnit.selectedItem.toString()
        val wUnit = spinWeightUnit.selectedItem.toString()

        if (hUnit == "in") {
            height = Conversion.INtoCM(height)
        } else if (hUnit == "feet") {
            height = Conversion.FTtoCM(height)
        }

        if (wUnit == "lb") {
            weight = Conversion.LBtoKG(weight)
        }

        val i = Intent(this, ActivitylevelActivity::class.java).apply {
            putExtras(intent.extras ?: Bundle())
            putExtra("Gender", genderStr)
            putExtra("Height", height)
            putExtra("Weight", weight)
        }
        startActivity(i)
    }
}
