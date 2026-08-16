package com.example.unyieldingmight.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unyieldingmight.models.User
import com.example.unyieldingmight.services.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginState>()
    val loginResult: LiveData<LoginState> = _loginResult

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            _loginResult.value = LoginState.Loading
            
            try {
                val user = withContext(Dispatchers.IO) {
                    Database.loginUser(email, passwordHash)
                }
                
                if (user != null) {
                    _loginResult.value = LoginState.Success(user)
                } else {
                    _loginResult.value = LoginState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginState.Error("A connection error occurred: ${e.message}")
            }
        }
    }
}

