package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityActivitylevelBinding

class ActivitylevelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActivitylevelBinding
    private var multiplier = 1.2f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivitylevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSeekBar()
        setupListeners()
    }

    private fun setupSeekBar() {
        binding.activityActivitylevelSbActivityLevel.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (progress) {
                    0 -> {
                        multiplier = 1.2f
                        binding.activityActivitylevelTvDescription.text = "Little to no exercise."
                    }
                    1 -> {
                        multiplier = 1.375f
                        binding.activityActivitylevelTvDescription.text = "Light exercise/sports 1-3 days/week."
                    }
                    2 -> {
                        multiplier = 1.55f
                        binding.activityActivitylevelTvDescription.text = "Moderate exercise/sports 3-5 days/week."
                    }
                    3 -> {
                        multiplier = 1.725f
                        binding.activityActivitylevelTvDescription.text = "Hard exercise/sports 6-7 days/week."
                    }
                    4 -> {
                        multiplier = 1.9f
                        binding.activityActivitylevelTvDescription.text = "Very hard exercise, physical job or training 2x/day."
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupListeners() {
        binding.activityActivitylevelBtnProceed.setOnClickListener {
            val intent = Intent(this, DoneActivity::class.java).apply {
                putExtras(this@ActivitylevelActivity.intent.extras ?: Bundle())
                putExtra("ActivityMultiplier", multiplier)
            }
            startActivity(intent)
        }

        binding.activityActivitylevelBtnBack.setOnClickListener {
            finish()
        }
    }
}

