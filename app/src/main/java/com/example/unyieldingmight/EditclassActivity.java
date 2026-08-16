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


public class EditclassActivity extends AppCompatActivity {
    Button activity_editclass_btn_datepicker = findViewById(R.id.activity_editclass_btn_datepicker);
    TextView activity_editclass_tv_selecteddate = findViewById(R.id.activity_editclass_tv_selecteddate);
    Button activity_editclass_btn_starttime = findViewById(R.id.activity_editclass_btn_starttime), activity_editclass_btn_endtime = findViewById(R.id.activity_editclass_btn_endtime);
    TextView activity_editclass_tv_startTtime = findViewById(R.id.activity_editclass_tv_startTtime), activity_editclass_tv_endtime = findViewById(R.id.activity_editclass_tv_endtime);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editclass);

        activity_editclass_btn_datepicker.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditclassActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        activity_editclass_tv_selecteddate.setText("Selected Date: " + date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        Calendar calendar = Calendar.getInstance();
        int currentHour   = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);



        activity_editclass_btn_starttime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    EditclassActivity.this,
                    (view, hourOfDay, minute) -> {
                        String time = formatTime(hourOfDay, minute);
                        activity_editclass_tv_startTtime.setText("Start Time: " + time);
                    },
                    currentHour, currentMinute, false
            );
            timePicker.setTitle("Select Start Time");
            timePicker.show();
        });

        activity_editclass_btn_endtime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    EditclassActivity.this,
                    (view, hourOfDay, minute) -> {
                        String time = formatTime(hourOfDay, minute);
                        activity_editclass_tv_endtime.setText("End Time: " + time);
                    },
                    currentHour, currentMinute, false
            );
            timePicker.setTitle("Select End Time");
            timePicker.show();
        });

        Spinner spinner = findViewById(R.id.activity_editclass_spinner_level);
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
