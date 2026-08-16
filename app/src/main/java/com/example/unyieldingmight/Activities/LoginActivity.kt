package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.Database

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.activity_login_et_email)
        passwordEditText = findViewById(R.id.activity_login_et_password)
        loginButton = findViewById(R.id.activity_login_btn_login)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        })
    }

    fun nextActivityRegister(v: View?) {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    fun login(v: View?) {
        val emailData = emailEditText.text.toString().trim()
        val passwordData = passwordEditText.text.toString().trim()

        if (emailData.isEmpty() || passwordData.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent multiple clicks
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."

        Thread {
            try {
                val user = Database.loginUser(emailData, passwordData)
                runOnUiThread {
                    loginButton.isEnabled = true
                    loginButton.setText(R.string.btn_login)
                    if (user != null) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val i = Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(i)
                    } else {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login error", e)
                runOnUiThread {
                    loginButton.isEnabled = true
                    loginButton.setText(R.string.btn_login)
                    Toast.makeText(this, "A connection error occurred", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
