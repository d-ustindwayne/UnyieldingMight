package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityEditclassBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditClassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditclassBinding
    private val viewModel: EditClassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditclassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.activityEditclassEtClassname.setText(intent.getStringExtra("Title"))
        binding.activityEditclassEtDate.setText(intent.getStringExtra("Date"))
        binding.activityEditclassEtStarttime.setText(intent.getStringExtra("StartTime"))
        binding.activityEditclassEtEndtime.setText(intent.getStringExtra("EndTime"))
        binding.activityEditclassEtMaxcapacity.setText(intent.getStringExtra("MaxCap"))
        binding.activityEditclassEtAvgcalorie.setText(intent.getFloatExtra("AvgCalorie", 0f).toString())
        binding.activityEditclassEtDescription.setText(intent.getStringExtra("Desc"))
    }

    private fun setupListeners() {
        binding.activityEditclassBtnDone.setOnClickListener {
            val updatedTitle = binding.activityEditclassEtClassname.text.toString().trim()
            val updatedDesc = binding.activityEditclassEtDescription.text.toString().trim()
            val dateString = binding.activityEditclassEtDate.text.toString().trim()
            val startTimeString = binding.activityEditclassEtStarttime.text.toString().trim()
            val endTimeString = binding.activityEditclassEtEndtime.text.toString().trim()
            val maxCapString = binding.activityEditclassEtMaxcapacity.text.toString().trim()
            val avgCalString = binding.activityEditclassEtAvgcalorie.text.toString().trim()

            val fields = listOf(updatedTitle, updatedDesc, dateString, startTimeString, endTimeString, maxCapString, avgCalString)
            if (fields.any { it.isEmpty() }) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dayMonthYear = dateString.split("/")
                val day = dayMonthYear[0].toInt()
                val month = dayMonthYear[1].toInt() - 1
                val year = dayMonthYear[2].toInt()

                val startTimeData = timeFormat.parse(startTimeString) ?: throw Exception()
                val startTimestamp = Timestamp(Calendar.getInstance().apply {
                    set(year, month, day)
                    val time = Calendar.getInstance().apply { time = startTimeData }
                    set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis)

                val endTimeData = timeFormat.parse(endTimeString) ?: throw Exception()
                val endTimestamp = Timestamp(Calendar.getInstance().apply {
                    set(year, month, day)
                    val time = Calendar.getInstance().apply { time = endTimeData }
                    set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis)

                viewModel.updateClass(
                    intent.getIntExtra("ClassId", 0),
                    updatedTitle, updatedDesc, startTimestamp, endTimestamp,
                    maxCapString.toInt(), avgCalString.toFloat()
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}. Please follow the format dd/MM/yyyy and HH:mm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.editState.observe(this) { state ->
            when (state) {
                is EditClassViewModel.EditState.Loading -> {
                    binding.activityEditclassBtnDone.isEnabled = false
                }
                is EditClassViewModel.EditState.Success -> {
                    Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show()
                    val intentHome = Intent(this, HomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intentHome)
                    finish()
                }
                is EditClassViewModel.EditState.Error -> {
                    binding.activityEditclassBtnDone.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

