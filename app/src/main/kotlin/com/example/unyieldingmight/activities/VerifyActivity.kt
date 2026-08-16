package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityVerifyBinding

class VerifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyBinding
    private val viewModel: VerifyViewModel by viewModels()
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("Email")
        if (email == null) {
            Toast.makeText(this, "Error: Email missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initOTPFields()
        setupListeners()
        observeViewModel()

        viewModel.startVerification(email!!)
    }

    private fun setupListeners() {
        binding.activityVerifyBtnConfirm.setOnClickListener {
            verify()
        }
    }

    private fun observeViewModel() {
        viewModel.verificationState.observe(this) { state ->
            when (state) {
                is VerifyViewModel.VerificationState.Loading -> {
                    Toast.makeText(this, "Verifying email security...", Toast.LENGTH_SHORT).show()
                }
                is VerifyViewModel.VerificationState.SecurityCheckPassed -> {
                    // Handled by OTPSent
                }
                is VerifyViewModel.VerificationState.SecurityCheckFailed -> {
                    Toast.makeText(this, "Security Check Failed: ${state.reason}", Toast.LENGTH_LONG).show()
                    finish()
                }
                is VerifyViewModel.VerificationState.OTPSent -> {
                    Toast.makeText(this, "Code sent to $email. Check your inbox.", Toast.LENGTH_LONG).show()
                }
                is VerifyViewModel.VerificationState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initOTPFields() {
        setupAutoMove(binding.activityVerifyEtNumber1, binding.activityVerifyEtNumber2)
        setupAutoMove(binding.activityVerifyEtNumber2, binding.activityVerifyEtNumber3)
        setupAutoMove(binding.activityVerifyEtNumber3, binding.activityVerifyEtNumber4)
        setupAutoMove(binding.activityVerifyEtNumber4, binding.activityVerifyEtNumber5)
        setupAutoMove(binding.activityVerifyEtNumber5, binding.activityVerifyEtNumber6)

        setupBackMove(binding.activityVerifyEtNumber2, binding.activityVerifyEtNumber1)
        setupBackMove(binding.activityVerifyEtNumber3, binding.activityVerifyEtNumber2)
        setupBackMove(binding.activityVerifyEtNumber4, binding.activityVerifyEtNumber3)
        setupBackMove(binding.activityVerifyEtNumber5, binding.activityVerifyEtNumber4)
        setupBackMove(binding.activityVerifyEtNumber6, binding.activityVerifyEtNumber5)
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

    private fun verify() {
        val inputOTP = binding.activityVerifyEtNumber1.text.toString().trim() +
                binding.activityVerifyEtNumber2.text.toString().trim() +
                binding.activityVerifyEtNumber3.text.toString().trim() +
                binding.activityVerifyEtNumber4.text.toString().trim() +
                binding.activityVerifyEtNumber5.text.toString().trim() +
                binding.activityVerifyEtNumber6.text.toString().trim()

        if (inputOTP.length < 6) {
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            if (viewModel.validateOTP(inputOTP)) {
                Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, com.example.unyieldingmight.activities.MembershipActivity::class.java).apply {
                    putExtras(this@VerifyActivity.intent.extras ?: Bundle())
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
