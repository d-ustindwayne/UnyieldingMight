package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.R

class ActivitylevelActivity : AppCompatActivity() {
    private lateinit var sbActivityLevel: SeekBar
    private lateinit var tvDescription: TextView
    private var multiplier = 1.2f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activitylevel)

        sbActivityLevel = findViewById(R.id.activity_activitylevel_sb_activityLevel)
        tvDescription = findViewById(R.id.activity_activitylevel_tv_description)

        sbActivityLevel.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (progress) {
                    0 -> {
                        multiplier = 1.2f
                        tvDescription.text = "Little to no exercise."
                    }
                    1 -> {
                        multiplier = 1.375f
                        tvDescription.text = "Light exercise/sports 1-3 days/week."
                    }
                    2 -> {
                        multiplier = 1.55f
                        tvDescription.text = "Moderate exercise/sports 3-5 days/week."
                    }
                    3 -> {
                        multiplier = 1.725f
                        tvDescription.text = "Hard exercise/sports 6-7 days/week."
                    }
                    4 -> {
                        multiplier = 1.9f
                        tvDescription.text = "Very hard exercise, physical job or training 2x/day."
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun nextActivityDone(v: View?) {
        val i = Intent(this, DoneActivity::class.java).apply {
            putExtras(intent.extras ?: Bundle())
            putExtra("ActivityMultiplier", multiplier)
        }
        startActivity(i)
    }

    fun previousActivityStats(v: View?) {
        finish()
    }
}
