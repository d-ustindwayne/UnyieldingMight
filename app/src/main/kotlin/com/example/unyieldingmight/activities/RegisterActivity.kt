package com.example.unyieldingmight.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unyieldingmight.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.activityRegisterBtnRegister.setOnClickListener {
            register()
        }

        binding.activityRegisterTvLoginNow.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterViewModel.RegisterState.Loading -> {
                    binding.activityRegisterBtnRegister.isEnabled = false
                }
                is RegisterViewModel.RegisterState.EmailCheckResult -> {
                    binding.activityRegisterBtnRegister.isEnabled = true
                    if (state.isAvailable) {
                        proceedToVerify()
                    } else {
                        val message = if (state.isAdmin) "Email is used by an admin" else "Email is already registered"
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
                is RegisterViewModel.RegisterState.Error -> {
                    binding.activityRegisterBtnRegister.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun register() {
        val email = binding.activityRegisterEtEmail.text.toString().trim()
        val password = binding.activityRegisterEtPassword.text.toString().trim()
        val confirmPassword = binding.activityRegisterEtConfirmPassword.text.toString().trim()
        val firstName = binding.activityRegisterEtFirstName.text.toString().trim()
        val lastName = binding.activityRegisterEtLastName.text.toString().trim()

        val fields = listOf(firstName, lastName, email, password, confirmPassword)
        
        if (fields.any { it.isEmpty() }) {
            Toast.makeText(this, "Input fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password is not the same", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.checkEmail(email)
    }

    private fun proceedToVerify() {
        val intent = Intent(this, VerifyActivity::class.java).apply {
            putExtra("FirstName", binding.activityRegisterEtFirstName.text.toString().trim())
            putExtra("LastName", binding.activityRegisterEtLastName.text.toString().trim())
            putExtra("Email", binding.activityRegisterEtEmail.text.toString().trim())
            putExtra("Password", binding.activityRegisterEtPassword.text.toString().trim())
        }
        startActivity(intent)
    }
}

