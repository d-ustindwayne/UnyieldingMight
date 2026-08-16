package com.example.unyieldingmight;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.app.DatePickerDialog;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import android.app.TimePickerDialog;

public class CreateclassActivity extends AppCompatActivity {

    Button activity_createclass_btn_datepicker;
    Button activity_createclass_btn_starttime, activity_createclass_btn_endtime;
    Button activity_createclass_btn_post;
    TextView activity_createclass_tv_selecteddate;
    TextView activity_createclass_tv_startTtime, activity_createclass_tv_endtime;
    EditText activity_createclass_et_classname;
    EditText activity_createclass_et_trainername;
    EditText activity_createclass_et_category;
    EditText activity_createclass_number_capacity;
    EditText activity_createclass_multiline_description;
    RadioButton activity_createclass_radiobutton_upcoming;
    RadioButton activity_createclass_radiobutton_upcoming2;
    RadioButton activity_createclass_radiobutton_upcoming3;
    Spinner activity_createclass_spinner_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createclass);

        activity_createclass_btn_datepicker      = findViewById(R.id.activity_createclass_btn_datepicker);
        activity_createclass_btn_starttime       = findViewById(R.id.activity_createclass_btn_starttime);
        activity_createclass_btn_endtime         = findViewById(R.id.activity_createclass_btn_endtime);
        activity_createclass_btn_post            = findViewById(R.id.activity_createclass_btn_post);
        activity_createclass_tv_selecteddate     = findViewById(R.id.activity_createclass_tv_selecteddate);
        activity_createclass_tv_startTtime       = findViewById(R.id.activity_createclass_tv_startTtime);
        activity_createclass_tv_endtime          = findViewById(R.id.activity_createclass_tv_endtime);
        activity_createclass_et_classname        = findViewById(R.id.activity_createclass_et_classname);
        activity_createclass_et_trainername      = findViewById(R.id.activity_createclass_et_trainername);
        activity_createclass_et_category         = findViewById(R.id.activity_createclass_et_category);
        activity_createclass_number_capacity     = findViewById(R.id.activity_createclass_number_capacity);
        activity_createclass_multiline_description = findViewById(R.id.activity_createclass_multiline_description);
        activity_createclass_radiobutton_upcoming  = findViewById(R.id.activity_createclass_radiobutton_upcoming);
        activity_createclass_radiobutton_upcoming2 = findViewById(R.id.activity_createclass_radiobutton_upcoming2);
        activity_createclass_radiobutton_upcoming3 = findViewById(R.id.activity_createclass_radiobutton_upcoming3);
        activity_createclass_spinner_level        = findViewById(R.id.activity_createclass_spinner_level);

        // Date picker in create class
        activity_createclass_btn_datepicker.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreateclassActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        activity_createclass_tv_selecteddate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Start & end time for create class
        Calendar calendar = Calendar.getInstance();
        int currentHour   = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        activity_createclass_btn_starttime.setOnClickListener(v -> {
            new TimePickerDialog(
                    CreateclassActivity.this,
                    (view, hourOfDay, minute) ->
                            activity_createclass_tv_startTtime.setText(formatTime(hourOfDay, minute)),
                    currentHour, currentMinute, false
            ).show();
        });

        activity_createclass_btn_endtime.setOnClickListener(v -> {
            new TimePickerDialog(
                    CreateclassActivity.this,
                    (view, hourOfDay, minute) ->
                            activity_createclass_tv_endtime.setText(formatTime(hourOfDay, minute)),
                    currentHour, currentMinute, false
            ).show();
        });

        // Spinner when selecting the intensity or level of the exercise
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.level_array, R.layout.my_selected_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity_createclass_spinner_level.setAdapter(adapter);

        // Post button
        activity_createclass_btn_post.setOnClickListener(v -> {
            // Get all values
            String className    = activity_createclass_et_classname.getText().toString().trim();
            String trainerName  = activity_createclass_et_trainername.getText().toString().trim();
            String category     = activity_createclass_et_category.getText().toString().trim();
            String capacity     = activity_createclass_number_capacity.getText().toString().trim();
            String level        = activity_createclass_spinner_level.getSelectedItem().toString();
            String description  = activity_createclass_multiline_description.getText().toString().trim();
            String date         = activity_createclass_tv_selecteddate.getText().toString();
            String startTime    = activity_createclass_tv_startTtime.getText().toString();
            String endTime      = activity_createclass_tv_endtime.getText().toString();

            // Get selected status
            String status = "";
            if (activity_createclass_radiobutton_upcoming.isChecked())  status = "Upcoming";
            else if (activity_createclass_radiobutton_upcoming2.isChecked()) status = "Ongoing";
            else if (activity_createclass_radiobutton_upcoming3.isChecked()) status = "Complete";

            // Get selected level
            if (activity_createclass_spinner_level.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a level", Toast.LENGTH_SHORT).show();
                boolean hasError = true;
            }

            // Validations
            if (className.isEmpty()) {
                Toast.makeText(this, "Please enter a class name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (trainerName.isEmpty()) {
                Toast.makeText(this, "Please enter a trainer name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (category.isEmpty()) {
                Toast.makeText(this, "Please enter a category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (capacity.isEmpty()) {
                Toast.makeText(this, "Please enter a capacity", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.equals("No date selected")) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (startTime.equals("No start time")) {
                Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (endTime.equals("No end time")) {
                Toast.makeText(this, "Please select an end time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (status.isEmpty()) {
                Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
                return;
            }
            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Class posted successfully", Toast.LENGTH_LONG).show();

        });
    }

    private String formatTime(int hourOfDay, int minute) {
        String am_pm = hourOfDay >= 12 ? "PM" : "AM";
        int hour = hourOfDay % 12;
        if (hour == 0) hour = 12;
        return String.format("%02d:%02d %s", hour, minute, am_pm);
    }
}