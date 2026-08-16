package com.example.unyieldingmight.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.unyieldingmight.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditclassActivity extends AppCompatActivity {

    Button edit;
    EditText classname, date, starttime, endtime, maxcapacity, description;

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
        description = findViewById(R.id.activity_editclass_et_description);

        Intent intent = getIntent();
        int classID = intent.getIntExtra("ClassId", 0);
        String title = intent.getStringExtra("Title");
        String classDate = intent.getStringExtra("Date");
        String classStartTime = intent.getStringExtra("StartTime");
        String classEndTime = intent.getStringExtra("EndTime");
        String classMxCapacity = intent.getStringExtra("MaxCap");
        String classDescription = intent.getStringExtra("Desc");

        classname.setText(title);
        date.setText(classDate);
        starttime.setText(classStartTime);
        endtime.setText(classEndTime);
        maxcapacity.setText(classMxCapacity);
        description.setText(classDescription);

        // Done button
        edit.setOnClickListener(v -> {
            String dateString = date.getText().toString().trim();
            String startTimeString = starttime.getText().toString().trim();
            String endTimeSstring = endtime.getText().toString().trim();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            try{
//                Date
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

                Date startDateTimeData = startDateTimeCalendar.getTime();

                Date endTimeData = timeFormat.parse(endTimeSstring);
                Calendar endDateTimeCalendar = Calendar.getInstance();
                endDateTimeCalendar.set(year, month, day);

                Calendar endTime = Calendar.getInstance();
                endTime.setTime(endTimeData);
                endDateTimeCalendar.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
                endDateTimeCalendar.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));
                endDateTimeCalendar.set(Calendar.SECOND, 0);
                endDateTimeCalendar.set(Calendar.MILLISECOND, 0);

                Date endDateTimeData = endDateTimeCalendar.getTime();

                Log.d(String.valueOf(startTimeData), "Start Time");
                Log.d(String.valueOf(endTimeData), "End Time");

                Log.d(String.valueOf(startDateTimeData), "Start Time Date");

                Log.d(String.valueOf(endDateTimeData), "End Time Date");

                Log.d(String.valueOf(classID), "Class ID");
            } catch (Exception e){
                Toast.makeText(this, "Please follow the format dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            }

        });
    }
}