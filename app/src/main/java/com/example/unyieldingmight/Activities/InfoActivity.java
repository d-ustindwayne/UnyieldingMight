package com.example.unyieldingmight.Activities;

import androidx.appcompat.app.AppCompatActivity;
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
    Button bookButton;
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
        bookButton = findViewById(R.id.activity_class_btn_book);

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

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (classId == -1) {
                    Toast.makeText(InfoActivity.this, "Error: Invalid Class ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                bookButton.setEnabled(false);
                bookButton.setText("Booking...");

                new Thread(() -> {
                    boolean success = Database.bookClass(classId);
                    runOnUiThread(() -> {
                        bookButton.setEnabled(true);
                        bookButton.setText("Book Now!");
                        if (success) {
                            Toast.makeText(InfoActivity.this, "Class booked successfully!", Toast.LENGTH_SHORT).show();
                            // Update UI capacity locally if needed
                            try {
                                int current = Integer.parseInt(infoCurCap.getText().toString());
                                infoCurCap.setText(String.valueOf(current + 1));
                            } catch (Exception e) {}
                        } else {
                            Toast.makeText(InfoActivity.this, "Booking failed. Class might be full or already booked.", Toast.LENGTH_LONG).show();
                        }
                    });
                }).start();
            }
        });
    }
}
