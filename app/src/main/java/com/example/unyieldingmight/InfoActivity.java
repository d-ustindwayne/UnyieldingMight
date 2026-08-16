package com.example.unyieldingmight;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

// For activity_class_info.xml
public class InfoActivity extends AppCompatActivity {
    ImageView infoImage;
    TextView infoTitle, infoInstructor, infoIntensity, infoDate, infoStartTime, infoEndTime, infoCurCap, infoMaxCap, infoDesc;
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

        Bundle bundle = getIntent().getExtras();
//        Ensures value is not empty
        if (bundle != null){
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
    }
}
