package com.example.unyieldingmight;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.app.DatePickerDialog;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import android.app.TimePickerDialog;

public class CreateclassActivity extends AppCompatActivity {
    Button btnDatePicker;
    TextView tvSelectedDate;
    Button btnStartTime, btnEndTime;
    TextView tvStartTime, tvEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createclass);

        btnDatePicker  = findViewById(R.id.btnDatePicker);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnStartTime   = findViewById(R.id.btnStartTime);
        btnEndTime     = findViewById(R.id.btnEndTime);
        tvStartTime    = findViewById(R.id.tvStartTime);
        tvEndTime      = findViewById(R.id.tvEndTime);

        Calendar calendar = Calendar.getInstance();
        int currentHour   = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        btnDatePicker.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreateclassActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        tvSelectedDate.setText("Selected Date: " + date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    CreateclassActivity.this,
                    (view, hourOfDay, minute) -> {
                        String time = formatTime(hourOfDay, minute);
                        tvStartTime.setText("Start Time: " + time);
                    },
                    currentHour, currentMinute, false
            );
            timePicker.setTitle("Select Start Time");
            timePicker.show();
        });

        btnEndTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    CreateclassActivity.this,
                    (view, hourOfDay, minute) -> {
                        String time = formatTime(hourOfDay, minute);
                        tvEndTime.setText("End Time: " + time);
                    },
                    currentHour, currentMinute, false
            );
            timePicker.setTitle("Select End Time");
            timePicker.show();
        });

        Spinner spinner = findViewById(R.id.activity_createclass_spinner_level);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.level_array,
                R.layout.my_selected_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private String formatTime(int hourOfDay, int minute) {
        String am_pm = hourOfDay >= 12 ? "PM" : "AM";
        int hour = hourOfDay % 12;
        if (hour == 0) hour = 12;
        return String.format("%02d:%02d %s", hour, minute, am_pm);
    }
}