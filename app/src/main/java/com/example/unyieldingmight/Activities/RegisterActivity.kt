package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.Database

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun nextActivityLogin(v: View?) {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
    }

    fun register(v: View?) {
        val firstName = findViewById<EditText>(R.id.activity_register_et_firstName).text.toString().trim()
        val lastName = findViewById<EditText>(R.id.activity_register_et_lastName).text.toString().trim()
        val emailData = findViewById<EditText>(R.id.activity_register_et_email).text.toString().trim()
        val passwordData = findViewById<EditText>(R.id.activity_register_et_password).text.toString().trim()
        val confirmPasswordData = findViewById<EditText>(R.id.activity_register_et_confirmPassword).text.toString().trim()

        val fields = listOf(firstName, lastName, emailData, passwordData, confirmPasswordData)
        
        if (fields.any { it.isEmpty() }) {
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailData).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
        } else if (passwordData != confirmPasswordData) {
            Toast.makeText(this, "Password is not the same", Toast.LENGTH_SHORT).show()
        } else {
            Thread {
                val existingCustomer = Database.getCustomer(emailData)
                runOnUiThread {
                    if (existingCustomer != null) {
                        val userClass = existingCustomer.profile.userClass
                        if ("ADMIN".equals(userClass, ignoreCase = true)) {
                            Toast.makeText(this, "Email is used by an admin", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Email is already registered", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Pass data to VerifyActivity
                        val i = Intent(this, VerifyActivity::class.java).apply {
                            putExtra("FirstName", firstName)
                            putExtra("LastName", lastName)
                            putExtra("Email", emailData)
                            putExtra("Password", passwordData)
                        }
                        startActivity(i)
                    }
                }
            }.start()
        }
    }
}
