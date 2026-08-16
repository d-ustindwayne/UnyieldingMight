package com.example.unyieldingmight.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.unyieldingmight.R;
import com.example.unyieldingmight.Services.Database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditClassActivity extends AppCompatActivity {

    Button edit;
    EditText classname, date, starttime, endtime, maxcapacity, avgcalorie, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editclass);

        edit = findViewById(R.id.activity_editclass_btn_done);

        classname = findViewById(R.id.activity_editclass_et_classname);
        date = findViewById(R.id.activity_editclass_et_date);
        starttime = findViewById(R.id.activity_editclass_et_starttime);
        endtime = findViewById(R.id.activity_editclass_et_endtime);
        maxcapacity = findViewById(R.id.activity_editclass_et_maxcapacity);
        avgcalorie = findViewById(R.id.activity_editclass_et_avgcalorie);
        description = findViewById(R.id.activity_editclass_et_description);

        Intent intent = getIntent();
        int classID = intent.getIntExtra("ClassId", 0);
        String title = intent.getStringExtra("Title");
        String classDate = intent.getStringExtra("Date");
        String classStartTime = intent.getStringExtra("StartTime");
        String classEndTime = intent.getStringExtra("EndTime");
        String classMxCapacity = intent.getStringExtra("MaxCap");
        String classDescription = intent.getStringExtra("Desc");
        float classAvgCalorie = intent.getFloatExtra("AvgCalorie", 0);

        classname.setText(title);
        date.setText(classDate);
        starttime.setText(classStartTime);
        endtime.setText(classEndTime);
        maxcapacity.setText(classMxCapacity);
        avgcalorie.setText(String.valueOf(classAvgCalorie));
        description.setText(classDescription);

        // Done button
        edit.setOnClickListener(v -> {
            String updatedTitle = classname.getText().toString().trim();
            String updatedDesc = description.getText().toString().trim();
            String dateString = date.getText().toString().trim();
            String startTimeString = starttime.getText().toString().trim();
            String endTimeString = endtime.getText().toString().trim();
            String maxCapString = maxcapacity.getText().toString().trim();
            String avgCalString = avgcalorie.getText().toString().trim();

            if (updatedTitle.isEmpty() || dateString.isEmpty() || startTimeString.isEmpty() || 
                endTimeString.isEmpty() || maxCapString.isEmpty() || avgCalString.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            try{
                String[] dayMonthYear = dateString.split("/");
                int day = Integer.parseInt(dayMonthYear[0]);
                int month = Integer.parseInt(dayMonthYear[1]) - 1;
                int year = Integer.parseInt(dayMonthYear[2]);

                Date startTimeData = timeFormat.parse(startTimeString);
                Calendar startDateTimeCalendar = Calendar.getInstance();
                startDateTimeCalendar.set(year, month, day);

                Calendar startTime = Calendar.getInstance();
                startTime.setTime(startTimeData);
                startDateTimeCalendar.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
                startDateTimeCalendar.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
                startDateTimeCalendar.set(Calendar.SECOND, 0);
                startDateTimeCalendar.set(Calendar.MILLISECOND, 0);

                java.sql.Timestamp startTimestamp = new java.sql.Timestamp(startDateTimeCalendar.getTimeInMillis());

                Date endTimeData = timeFormat.parse(endTimeString);
                Calendar endDateTimeCalendar = Calendar.getInstance();
                endDateTimeCalendar.set(year, month, day);

                Calendar endTime = Calendar.getInstance();
                endTime.setTime(endTimeData);
                endDateTimeCalendar.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
                endDateTimeCalendar.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));
                endDateTimeCalendar.set(Calendar.SECOND, 0);
                endDateTimeCalendar.set(Calendar.MILLISECOND, 0);

                java.sql.Timestamp endTimestamp = new java.sql.Timestamp(endDateTimeCalendar.getTimeInMillis());

                int maxCap = Integer.parseInt(maxCapString);
                float avgCal = Float.parseFloat(avgCalString);

                new Thread(() -> {
                    boolean success = Database.editClass(classID, updatedTitle, updatedDesc, startTimestamp, endTimestamp, maxCap, avgCal);
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show();
                            Intent intentHome = new Intent(this, HomeActivity.class);
                            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intentHome);
                        } else {
                            Toast.makeText(this, "Failed to update class", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();

            } catch (Exception e){
                Toast.makeText(this, "Please follow the format dd/MM/yyyy and HH:mm", Toast.LENGTH_SHORT).show();
            }

        });
    }
}