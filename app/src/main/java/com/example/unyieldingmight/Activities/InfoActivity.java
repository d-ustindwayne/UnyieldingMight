package com.example.unyieldingmight.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unyieldingmight.Services.Database;
import com.example.unyieldingmight.R;

// For activity_class_info.xml
public class InfoActivity extends AppCompatActivity {
    ImageView infoImage;
    TextView infoTitle, infoInstructor, infoIntensity, infoDate, infoStartTime, infoEndTime, infoCurCap, infoMaxCap, infoDesc;
    Button clickButton;
    int classId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_info);

        infoImage = findViewById(R.id.activity_class_iv_class);
        infoTitle = findViewById(R.id.activity_class_tv_title);
        infoInstructor = findViewById(R.id.activity_class_tv_instructor);
        infoIntensity = findViewById(R.id.activity_class_tv_intensity);
        infoDate = findViewById(R.id.activity_class_tv_date);
        infoStartTime = findViewById(R.id.activity_class_tv_startTime);
        infoEndTime = findViewById(R.id.activity_class_tv_endTime);
        infoCurCap = findViewById(R.id.activity_class_tv_currentCapacity);
        infoMaxCap = findViewById(R.id.activity_class_tv_maxCapacity);
        infoDesc = findViewById(R.id.activity_class_tv_description);
        clickButton = findViewById(R.id.activity_class_btn_book);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            classId = bundle.getInt("ClassId", -1);
            infoImage.setImageResource(bundle.getInt("Image"));
            infoTitle.setText(bundle.getString("Title"));
            infoInstructor.setText(bundle.getString("Instructor"));
            infoIntensity.setText(bundle.getString("Intensity"));
            infoDate.setText(bundle.getString("Date"));
            infoStartTime.setText(bundle.getString("StartTime"));
            infoEndTime.setText(bundle.getString("EndTime"));
            infoCurCap.setText(bundle.getString("CurCap"));
            infoMaxCap.setText(bundle.getString("MaxCap"));
            infoDesc.setText(bundle.getString("Desc"));
        }

        String user = Database.getCurrentUser().getUserClass().toString();
        if (user.equals("ADMIN")) {
            clickButton.setText("Edit Class");
            clickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(() -> {
                        Intent intent = getIntent();
                        int classID = intent.getIntExtra("ClassId", 0);
                        String title = intent.getStringExtra("Title");
                        String date = intent.getStringExtra("Date");
                        String startTime = intent.getStringExtra("StartTime");
                        String endTime = intent.getStringExtra("EndTime");
                        String maxCap = intent.getStringExtra("MaxCap");
                        String intensity = intent.getStringExtra("Intensity");
                        String desc = intent.getStringExtra("Desc");
                        float avgCalorie = intent.getFloatExtra("AvgCalorie", 200);

                        Intent i = new Intent(InfoActivity.this, EditClassActivity.class);
                        i.putExtra("ClassId", classID);
                        i.putExtra("Title", title);
                        i.putExtra("Date", date);
                        i.putExtra("StartTime", startTime);
                        i.putExtra("EndTime", endTime);
                        i.putExtra("MaxCap", maxCap);
                        i.putExtra("Intensity", intensity);
                        i.putExtra("Desc", desc);
                        i.putExtra("AvgCalorie", avgCalorie);
                        startActivity(i);
                    }).start();
                }
            });
        } else {
            clickButton.setText("Book Now!");
            clickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (classId == -1) {
                        Toast.makeText(InfoActivity.this, "Error: Invalid Class ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    clickButton.setEnabled(false);
                    clickButton.setText("Booking...");

                    new Thread(() -> {
                        boolean alreadyBooked = Database.isAlreadyBooked(classId);
                        if (alreadyBooked) {
                            runOnUiThread(() -> {
                                clickButton.setEnabled(true);
                                clickButton.setText("Book Now!");
                                Toast.makeText(InfoActivity.this, "You have already booked this class.", Toast.LENGTH_LONG).show();
                            });
                            return;
                        }

                        boolean success = Database.bookClass(classId);
                        runOnUiThread(() -> {
                            clickButton.setEnabled(true);
                            clickButton.setText("Book Now!");
                            if (success) {
                                Toast.makeText(InfoActivity.this, "Class booked successfully!", Toast.LENGTH_SHORT).show();
                                try {
                                    int current = Integer.parseInt(infoCurCap.getText().toString());
                                    infoCurCap.setText(String.valueOf(current + 1));
                                    finish();
                                } catch (Exception e) {}
                            } else {
                                Toast.makeText(InfoActivity.this, "Booking failed. Class might be full.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }).start();
                }
            });
        }
    }
}
