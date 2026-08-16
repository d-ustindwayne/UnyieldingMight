package com.example.unyieldingmight.activities

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.models.User
import com.example.unyieldingmight.databinding.ActivityClassInfoBinding

class InfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassInfoBinding
    private val viewModel: InfoViewModel by viewModels()
    private var classId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        intent.extras?.let { bundle ->
            classId = bundle.getInt("ClassId", -1)
            binding.activityClassIvClass.setImageResource(bundle.getInt("Image"))
            binding.activityClassTvTitle.text = bundle.getString("Title")
            binding.activityClassTvInstructor.text = bundle.getString("Instructor")
            binding.activityClassTvIntensity.text = bundle.getString("Intensity")
            binding.activityClassTvDate.text = bundle.getString("Date")
            binding.activityClassTvStartTime.text = bundle.getString("StartTime")
            binding.activityClassTvEndTime.text = bundle.getString("EndTime")
            binding.activityClassTvCurrentCapacity.text = bundle.getString("CurCap")
            binding.activityClassTvMaxCapacity.text = bundle.getString("MaxCap")
            binding.activityClassTvDescription.text = bundle.getString("Desc")
        }

        if (viewModel.getCurrentUserRole() == User.Role.ADMIN) {
            binding.activityClassBtnBook.text = "Edit Class"
        } else {
            binding.activityClassBtnBook.text = "Book Now!"
        }
    }

    private fun setupListeners() {
        binding.activityClassBtnBook.setOnClickListener {
            if (viewModel.getCurrentUserRole() == User.Role.ADMIN) {
                val i = Intent(this, EditClassActivity::class.java).apply {
                    putExtra("ClassId", intent.getIntExtra("ClassId", 0))
                    putExtra("Title", intent.getStringExtra("Title"))
                    putExtra("Date", intent.getStringExtra("Date"))
                    putExtra("StartTime", intent.getStringExtra("StartTime"))
                    putExtra("EndTime", intent.getStringExtra("EndTime"))
                    putExtra("MaxCap", intent.getStringExtra("MaxCap"))
                    putExtra("Intensity", intent.getStringExtra("Intensity"))
                    putExtra("Desc", intent.getStringExtra("Desc"))
                    putExtra("AvgCalorie", intent.getFloatExtra("AvgCalorie", 200f))
                }
                startActivity(i)
            } else {
                if (classId == -1) {
                    Toast.makeText(this, "Error: Invalid Class ID", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.bookClass(classId)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.bookingState.observe(this) { state ->
            when (state) {
                is InfoViewModel.BookingState.Loading -> {
                    binding.activityClassBtnBook.isEnabled = false
                    binding.activityClassBtnBook.text = "Booking..."
                }
                is InfoViewModel.BookingState.Success -> {
                    binding.activityClassBtnBook.isEnabled = true
                    binding.activityClassBtnBook.text = "Book Now!"
                    Toast.makeText(this, "Class booked successfully!", Toast.LENGTH_SHORT).show()
                    try {
                        val current = binding.activityClassTvCurrentCapacity.text.toString().toInt()
                        binding.activityClassTvCurrentCapacity.text = (current + 1).toString()
                        finish()
                    } catch (e: Exception) {
                        Log.e("InfoActivity", "Error updating capacity: ${e.message}")
                        finish()
                    }
                }
                is InfoViewModel.BookingState.AlreadyBooked -> {
                    binding.activityClassBtnBook.isEnabled = true
                    binding.activityClassBtnBook.text = "Book Now!"
                    Toast.makeText(this, "You have already booked this class.", Toast.LENGTH_LONG).show()
                }
                is InfoViewModel.BookingState.Error -> {
                    binding.activityClassBtnBook.isEnabled = true
                    binding.activityClassBtnBook.text = "Book Now!"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    finish()
                }
                else -> {}
            }
        }
    }
}

