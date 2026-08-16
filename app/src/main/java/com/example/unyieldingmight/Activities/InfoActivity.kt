package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.Models.User
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.Database

class InfoActivity : AppCompatActivity() {
    private lateinit var infoImage: ImageView
    private lateinit var infoTitle: TextView
    private lateinit var infoInstructor: TextView
    private lateinit var infoIntensity: TextView
    private lateinit var infoDate: TextView
    private lateinit var infoStartTime: TextView
    private lateinit var infoEndTime: TextView
    private lateinit var infoCurCap: TextView
    private lateinit var infoMaxCap: TextView
    private lateinit var infoDesc: TextView
    private lateinit var clickButton: Button
    private var classId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_info)

        infoImage = findViewById(R.id.activity_class_iv_class)
        infoTitle = findViewById(R.id.activity_class_tv_title)
        infoInstructor = findViewById(R.id.activity_class_tv_instructor)
        infoIntensity = findViewById(R.id.activity_class_tv_intensity)
        infoDate = findViewById(R.id.activity_class_tv_date)
        infoStartTime = findViewById(R.id.activity_class_tv_startTime)
        infoEndTime = findViewById(R.id.activity_class_tv_endTime)
        infoCurCap = findViewById(R.id.activity_class_tv_currentCapacity)
        infoMaxCap = findViewById(R.id.activity_class_tv_maxCapacity)
        infoDesc = findViewById(R.id.activity_class_tv_description)
        clickButton = findViewById(R.id.activity_class_btn_book)

        intent.extras?.let { bundle ->
            classId = bundle.getInt("ClassId", -1)
            infoImage.setImageResource(bundle.getInt("Image"))
            infoTitle.text = bundle.getString("Title")
            infoInstructor.text = bundle.getString("Instructor")
            infoIntensity.text = bundle.getString("Intensity")
            infoDate.text = bundle.getString("Date")
            infoStartTime.text = bundle.getString("StartTime")
            infoEndTime.text = bundle.getString("EndTime")
            infoCurCap.text = bundle.getString("CurCap")
            infoMaxCap.text = bundle.getString("MaxCap")
            infoDesc.text = bundle.getString("Desc")
        }

        val userRole = Database.getCurrentUser()?.userClass
        if (userRole == User.Role.ADMIN) {
            clickButton.text = "Edit Class"
            clickButton.setOnClickListener {
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
            }
        } else {
            clickButton.text = "Book Now!"
            clickButton.setOnClickListener {
                if (classId == -1) {
                    Toast.makeText(this, "Error: Invalid Class ID", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                clickButton.isEnabled = false
                clickButton.text = "Booking..."

                Thread {
                    val alreadyBooked = Database.isAlreadyBooked(classId)
                    if (alreadyBooked) {
                        runOnUiThread {
                            clickButton.isEnabled = true
                            clickButton.text = "Book Now!"
                            Toast.makeText(this, "You have already booked this class.", Toast.LENGTH_LONG).show()
                        }
                        return@Thread
                    }

                    val success = Database.bookClass(classId)
                    runOnUiThread {
                        clickButton.isEnabled = true
                        clickButton.text = "Book Now!"
                        if (success) {
                            Toast.makeText(this, "Class booked successfully!", Toast.LENGTH_SHORT).show()
                            try {
                                val current = infoCurCap.text.toString().toInt()
                                infoCurCap.text = (current + 1).toString()
                                finish()
                            } catch (e: Exception) {
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Booking failed. Class might be full.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }.start()
            }
        }
    }
}
