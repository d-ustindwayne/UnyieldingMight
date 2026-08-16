package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.Database
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditClassActivity : AppCompatActivity() {
    private lateinit var edit: Button
    private lateinit var classname: EditText
    private lateinit var date: EditText
    private lateinit var starttime: EditText
    private lateinit var endtime: EditText
    private lateinit var maxcapacity: EditText
    private lateinit var avgcalorie: EditText
    private lateinit var description: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editclass)

        edit = findViewById(R.id.activity_editclass_btn_done)
        classname = findViewById(R.id.activity_editclass_et_classname)
        date = findViewById(R.id.activity_editclass_et_date)
        starttime = findViewById(R.id.activity_editclass_et_starttime)
        endtime = findViewById(R.id.activity_editclass_et_endtime)
        maxcapacity = findViewById(R.id.activity_editclass_et_maxcapacity)
        avgcalorie = findViewById(R.id.activity_editclass_et_avgcalorie)
        description = findViewById(R.id.activity_editclass_et_description)

        val classID = intent.getIntExtra("ClassId", 0)
        classname.setText(intent.getStringExtra("Title"))
        date.setText(intent.getStringExtra("Date"))
        starttime.setText(intent.getStringExtra("StartTime"))
        endtime.setText(intent.getStringExtra("EndTime"))
        maxcapacity.setText(intent.getStringExtra("MaxCap"))
        avgcalorie.setText(intent.getFloatExtra("AvgCalorie", 0f).toString())
        description.setText(intent.getStringExtra("Desc"))

        edit.setOnClickListener {
            val updatedTitle = classname.text.toString().trim()
            val updatedDesc = description.text.toString().trim()
            val dateString = date.text.toString().trim()
            val startTimeString = starttime.text.toString().trim()
            val endTimeString = endtime.text.toString().trim()
            val maxCapString = maxcapacity.text.toString().trim()
            val avgCalString = avgcalorie.text.toString().trim()

            val fields = listOf(updatedTitle, updatedDesc, dateString, startTimeString, endTimeString, maxCapString, avgCalString)
            if (fields.any { it.isEmpty() }) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            try {
                val dayMonthYear = dateString.split("/")
                val day = dayMonthYear[0].toInt()
                val month = dayMonthYear[1].toInt() - 1
                val year = dayMonthYear[2].toInt()

                val startTimeData = timeFormat.parse(startTimeString) ?: throw Exception("Invalid start time")
                val startDateTimeCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                    val time = Calendar.getInstance().apply { time = startTimeData }
                    set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val startTimestamp = Timestamp(startDateTimeCalendar.timeInMillis)

                val endTimeData = timeFormat.parse(endTimeString) ?: throw Exception("Invalid end time")
                val endDateTimeCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                    val time = Calendar.getInstance().apply { time = endTimeData }
                    set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val endTimestamp = Timestamp(endDateTimeCalendar.timeInMillis)

                val maxCap = maxCapString.toInt()
                val avgCal = avgCalString.toFloat()

                Thread {
                    val success = Database.editClass(
                        classID,
                        updatedTitle,
                        updatedDesc,
                        startTimestamp,
                        endTimestamp,
                        maxCap,
                        avgCal
                    )
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show()
                            val intentHome = Intent(this, HomeActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            startActivity(intentHome)
                        } else {
                            Toast.makeText(this, "Failed to update class", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } catch (e: Exception) {
                Toast.makeText(this, "Please follow the format dd/MM/yyyy and HH:mm", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
