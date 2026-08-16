package com.example.unyieldingmight.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unyieldingmight.models.EmailVerificationData
import com.example.unyieldingmight.services.EmailOTP
import com.example.unyieldingmight.services.EmailVerification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifyViewModel : ViewModel() {

    private val _verificationState = MutableLiveData<VerificationState>()
    val verificationState: LiveData<VerificationState> = _verificationState

    private val emailOTP = EmailOTP()
    val expectedOTP: String get() = emailOTP.OTP

    sealed class VerificationState {
        object Loading : VerificationState()
        object SecurityCheckPassed : VerificationState()
        data class SecurityCheckFailed(val reason: String) : VerificationState()
        object OTPSent : VerificationState()
        data class Error(val message: String) : VerificationState()
    }

    fun startVerification(email: String) {
        viewModelScope.launch {
            _verificationState.value = VerificationState.Loading
            
            try {
                val data: EmailVerificationData? = withContext(Dispatchers.IO) {
                    EmailVerification().email(email).verify()?.data
                }
                
                if (data != null && data.success.equals("true", ignoreCase = true)) {
                    val isSafe = data.safe_to_send.equals("true", ignoreCase = true)
                    val isValid = data.result.equals("valid", ignoreCase = true)
                    val isDisposable = data.disposable.equals("true", ignoreCase = true)

                    if ((isSafe || isValid) && !isDisposable) {
                        _verificationState.value = VerificationState.SecurityCheckPassed
                        sendOTP(email)
                    } else {
                        _verificationState.value = VerificationState.SecurityCheckFailed(data.reason ?: "Invalid email")
                    }
                } else {
                    sendOTP(email)
                }
            } catch (e: Exception) {
                Log.e("VerifyViewModel", "Verification error: ${e.message}")
                sendOTP(email)
            }
        }
    }

    private fun sendOTP(email: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    emailOTP.send(email)
                }
                _verificationState.value = VerificationState.OTPSent
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error("Failed to send OTP: ${e.message}")
            }
        }
    }

    fun validateOTP(input: String): Boolean {
        return emailOTP.validateOTP(input)
    }
}
