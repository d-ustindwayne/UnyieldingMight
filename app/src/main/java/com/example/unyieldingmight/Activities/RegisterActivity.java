package com.example.unyieldingmight.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unyieldingmight.Models.Customer;
import com.example.unyieldingmight.Services.Database;
import com.example.unyieldingmight.R;

import java.util.stream.Stream;

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

    public void register(View v){
        EditText etFirstName = findViewById(R.id.activity_register_et_firstName);
        String firstName = etFirstName.getText().toString().trim();

        EditText etLastName = findViewById(R.id.activity_register_et_lastName);
        String lastName = etLastName.getText().toString().trim();

        EditText email = findViewById(R.id.activity_register_et_email);
        String emailData = email.getText().toString().trim();

        EditText password = findViewById(R.id.activity_register_et_password);
        String passwordData = password.getText().toString().trim();

        EditText confirmPassword = findViewById(R.id.activity_register_et_confirmPassword);
        String confirmPasswordData = confirmPassword.getText().toString().trim();

//        if(firstName.isEmpty() || lastName.isEmpty() || emailData.isEmpty() || passwordData.isEmpty() || confirmPasswordData.isEmpty()){
//            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show();
//        }
        boolean isEmpty = Stream.of(firstName, lastName, emailData, passwordData, confirmPasswordData).anyMatch(String::isEmpty);
        if(isEmpty){
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailData).matches()){
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
        }
        else if(!passwordData.equals(confirmPasswordData)){
            Toast.makeText(this, "Password is not the same", Toast.LENGTH_SHORT).show();
        }
        else{
            new Thread(() -> {
                Customer existingCustomer = Database.getCustomer(emailData);
                
                runOnUiThread(() -> {
                    if (existingCustomer != null) {
                        String userClass = existingCustomer.getProfile().getUserClass();
                        if ("ADMIN".equalsIgnoreCase(userClass)) {
                            Toast.makeText(this, "Email is used by an admin", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Email is already registered", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Pass data to VerifyActivity
                        Intent i = new Intent(this, VerifyActivity.class);
                        i.putExtra("FirstName", firstName);
                        i.putExtra("LastName", lastName);
                        i.putExtra("Email", emailData);
                        i.putExtra("Password", passwordData);
                        startActivity(i);
                    }
                });
            }).start();
        }
    }
}
