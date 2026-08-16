package com.example.unyieldingmight.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unyieldingmight.services.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class EmailCheckResult(val isAvailable: Boolean, val isAdmin: Boolean) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    fun checkEmail(email: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            
            try {
                val customer = withContext(Dispatchers.IO) {
                    Database.getCustomer(email)
                }
                
                if (customer != null) {
                    val isAdmin = "ADMIN".equals(customer.profile.userClass, ignoreCase = true)
                    _registerState.value = RegisterState.EmailCheckResult(isAvailable = false, isAdmin = isAdmin)
                } else {
                    _registerState.value = RegisterState.EmailCheckResult(isAvailable = true, isAdmin = false)
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Database connection error: ${e.message}")
            }
        }
    }
}

