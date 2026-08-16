package com.example.unyieldingmight.Activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.R
import com.example.unyieldingmight.Services.EmailOTP
import com.example.unyieldingmight.Services.EmailVerification

class VerifyActivity : AppCompatActivity() {
    private var email: String? = null
    private var password: String? = null
    private var emailOTP: EmailOTP? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        email = intent.getStringExtra("Email")
        password = intent.getStringExtra("Password")

        if (email == null) {
            Toast.makeText(this, "Error: Email missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initOtpFields()
        emailOTP = EmailOTP()

        Toast.makeText(this, "Verifying email security...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                val verification = EmailVerification().email(email).verify()
                val data = verification?.data
                val rawResponse = verification?.getResponseString()

                runOnUiThread {
                    if (data != null && data.success.equals("true", ignoreCase = true)) {
                        val isSafe = data.safe_to_send.equals("true", ignoreCase = true)
                        val isValid = data.result.equals("valid", ignoreCase = true)
                        val isDisposable = data.disposable.equals("true", ignoreCase = true)

                        if ((isSafe || isValid) && !isDisposable) {
                            sendOTP()
                        } else {
                            val reason = data.reason ?: "Invalid or risky email"
                            Toast.makeText(this, "Security Check Failed: $reason", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    } else {
                        if (rawResponse?.contains("invalid_api_key") == true) {
                            Toast.makeText(this, "Verification API error: Invalid API Key.", Toast.LENGTH_SHORT).show()
                        }
                        sendOTP()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in verification thread", e)
                runOnUiThread { sendOTP() }
            }
        }.start()
    }

    private fun initOtpFields() {
        val otp1 = findViewById<EditText>(R.id.activity_verify_et_number1)
        val otp2 = findViewById<EditText>(R.id.activity_verify_et_number2)
        val otp3 = findViewById<EditText>(R.id.activity_verify_et_number3)
        val otp4 = findViewById<EditText>(R.id.activity_verify_et_number4)
        val otp5 = findViewById<EditText>(R.id.activity_verify_et_number5)
        val otp6 = findViewById<EditText>(R.id.activity_verify_et_number6)

        setupAutoMove(otp1, otp2)
        setupAutoMove(otp2, otp3)
        setupAutoMove(otp3, otp4)
        setupAutoMove(otp4, otp5)
        setupAutoMove(otp5, otp6)

        setupBackMove(otp2, otp1)
        setupBackMove(otp3, otp2)
        setupBackMove(otp4, otp3)
        setupBackMove(otp5, otp4)
        setupBackMove(otp6, otp5)
    }

    private fun setupAutoMove(current: EditText, next: EditText) {
        current.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 1) next.requestFocus()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBackMove(current: EditText, previous: EditText) {
        current.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (current.text.toString().isEmpty()) {
                    previous.requestFocus()
                }
            }
            false
        }
    }

    private fun sendOTP() {
        Toast.makeText(this, "Sending verification code...", Toast.LENGTH_SHORT).show()
        Thread {
            try {
                emailOTP?.send(email!!)
                // We don't have direct access to responseString from EmailOTP anymore, 
                // but EmailFunction logs errors.
                runOnUiThread {
                    Toast.makeText(this, "Code sent to $email. Check your inbox (or spam).", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in send thread", e)
                runOnUiThread {
                    Toast.makeText(this, "Internal error sending mail.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun verify(v: View?) {
        val otp1 = findViewById<EditText>(R.id.activity_verify_et_number1)
        val otp2 = findViewById<EditText>(R.id.activity_verify_et_number2)
        val otp3 = findViewById<EditText>(R.id.activity_verify_et_number3)
        val otp4 = findViewById<EditText>(R.id.activity_verify_et_number4)
        val otp5 = findViewById<EditText>(R.id.activity_verify_et_number5)
        val otp6 = findViewById<EditText>(R.id.activity_verify_et_number6)

        val inputOtp = otp1.text.toString().trim() +
                otp2.text.toString().trim() +
                otp3.text.toString().trim() +
                otp4.text.toString().trim() +
                otp5.text.toString().trim() +
                otp6.text.toString().trim()

        if (inputOtp.length < 6) {
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            if (emailOTP?.validateOTP(inputOtp) == true) {
                Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show()
                val i = Intent(this, MembershipActivity::class.java).apply {
                    putExtras(intent.extras ?: Bundle())
                }
                startActivity(i)
            } else {
                Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "VerifyActivity"
    }
}
