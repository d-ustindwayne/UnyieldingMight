package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
    }

    public void verify(View v){
        EditText otp1 = findViewById(R.id.activity_verify_et_number1);
        String otp1Data = otp1.getText().toString().trim();

        EditText otp2 = findViewById(R.id.activity_verify_et_number2);
        String otp2Data = otp2.getText().toString().trim();

        EditText otp3 = findViewById(R.id.activity_verify_et_number3);
        String otp3Data = otp3.getText().toString().trim();

        EditText otp4 = findViewById(R.id.activity_verify_et_number4);
        String otp4Data = otp4.getText().toString().trim();

        EditText otp5 = findViewById(R.id.activity_verify_et_number5);
        String otp5Data = otp5.getText().toString().trim();

        EditText otp6 = findViewById(R.id.activity_verify_et_number6);
        String otp6Data = otp6.getText().toString().trim();

        if(otp1Data.isEmpty() || otp2Data.isEmpty() || otp3Data.isEmpty() || otp4Data.isEmpty() || otp5Data.isEmpty() || otp6Data.isEmpty()){
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
//            Otp num (String)
            String otp = otp1Data + otp2Data + otp3Data + otp4Data + otp5Data + otp6Data;
            Intent i = new Intent(this, MembershipActivity.class);
            Log.d(otp, "Otp");
            startActivity(i);
        }
    }
}
