package com.example.unyieldingmight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void nextActivityLogin(View v){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

// Register function
    public void register(View v){
//        Finds the input field from activity_register.xml and assigns them to a variable
        EditText email = findViewById(R.id.activity_register_et_email);
        String emailData = email.getText().toString().trim();

        EditText password = findViewById(R.id.activity_register_et_password);
        String passwordData = password.getText().toString().trim();

        EditText confirmPassword = findViewById(R.id.activity_register_et_confirmPassword);
        String confirmPasswordData = confirmPassword.getText().toString().trim();

        if(emailData.isEmpty() || passwordData.isEmpty() || confirmPasswordData.isEmpty()){
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if(!passwordData.equals(confirmPasswordData)){
            Toast.makeText(this, "Password is not the same", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent i = new Intent(this, VerifyActivity.class);
            i.putExtra(emailData,"Email");
            i.putExtra(passwordData, "Password");

            startActivity(i);
        }
    }
}
