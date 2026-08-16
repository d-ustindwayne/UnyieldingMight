package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.utils.Conversion
import com.example.unyieldingmight.databinding.ActivityStatsBinding
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupListeners()
    }

    private fun setupSpinners() {
        val adapterHeight = ArrayAdapter.createFromResource(
            this,
            com.example.unyieldingmight.R.array.height_array, android.R.layout.simple_spinner_item
        )
        adapterHeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityStatsSpinHeight.adapter = adapterHeight

        val adapterWeight = ArrayAdapter.createFromResource(
            this,
            com.example.unyieldingmight.R.array.weight_array, android.R.layout.simple_spinner_item
        )
        adapterWeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityStatsSpinWeight.adapter = adapterWeight

        val adapterGender = ArrayAdapter.createFromResource(
            this,
            com.example.unyieldingmight.R.array.gender_array, android.R.layout.simple_spinner_item
        )
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityStatsSpinGender.adapter = adapterGender
    }

    private fun setupListeners() {
        binding.activityStatsBtnProceed.setOnClickListener {
            proceedToActivityLevel()
        }

        binding.activityStatsBtnPreviousActivity.setOnClickListener {
            finish()
        }
    }

    private fun proceedToActivityLevel() {
        val genderStr = binding.activityStatsSpinGender.selectedItem.toString().uppercase(Locale.getDefault())
        val heightStr = binding.activityStatsEtHeight.text.toString().trim()
        val weightStr = binding.activityStatsEtWeight.text.toString().trim()

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter your height and weight", Toast.LENGTH_SHORT).show()
            return
        }

        var height = heightStr.toFloat()
        var weight = weightStr.toFloat()

        val hUnit = binding.activityStatsSpinHeight.selectedItem.toString()
        val wUnit = binding.activityStatsSpinWeight.selectedItem.toString()

        if (hUnit == "in") {
            height = Conversion.INtoCM(height)
        } else if (hUnit == "feet") {
            height = Conversion.FTtoCM(height)
        }

        if (wUnit == "lb") {
            weight = Conversion.LBtoKG(weight)
        }

        val intent = Intent(this, ActivitylevelActivity::class.java).apply {
            putExtras(this@StatsActivity.intent.extras ?: Bundle())
            putExtra("Gender", genderStr)
            putExtra("Height", height)
            putExtra("Weight", weight)
        }
        startActivity(intent)
    }
}

