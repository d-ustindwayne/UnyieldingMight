package com.example.unyieldingmight.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unyieldingmight.models.GymClass
import com.example.unyieldingmight.services.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _gymClasses = MutableLiveData<List<GymClass>>()
    val gymClasses: LiveData<List<GymClass>> = _gymClasses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _userTDEE = MutableLiveData<Float>()
    val userTDEE: LiveData<Float> = _userTDEE

    fun loadGymClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = withContext(Dispatchers.IO) {
                    val current = Database.getCurrentUser()
                    var TDEE = 2000f
                    if (current != null) {
                        val customer = Database.getCustomer(current.email)
                        if (customer != null) {
                            TDEE = customer.TDEE
                            if (TDEE <= 0) TDEE = customer.calculateTDEE()
                        }
                    }
                    val classes = Database.gymClassesAvailable
                    Pair(classes, TDEE)
                }
                
                _userTDEE.value = result.second
                _gymClasses.value = result.first
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error connecting to database: ${e.message}"
            }
        }
    }
}

