package com.example.unyieldingmight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// For activity_class_info.xml
public class InfoActivity extends AppCompatActivity {
    ImageView infoImage;
    TextView infoTitle, infoInstructor, infoIntensity, infoDate, infoStartTime, infoEndTime, infoCurCap, infoMaxCap, infoDesc;
    Button infoButton;

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
        infoButton = findViewById(R.id.activity_class_btn_book);

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

        User.Role user = Database.getCurrentUser().getUserClass();
        Log.d(String.valueOf(user), "User type");
        if(String.valueOf(user).equals("ADMIN")){
            infoButton.setText("Edit Class");
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(InfoActivity.this,"Button works", Toast.LENGTH_SHORT).show();;
                    new Thread(() -> {
                        Intent intent = getIntent();
                        int classID = intent.getIntExtra("ClassId", 0);
                        Log.d(String.valueOf(classID), "ClassID");
                    }).start();
                }
            });
        }
        else{
            return;
        }

    }
}
