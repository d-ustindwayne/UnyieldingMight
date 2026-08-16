package com.example.unyieldingmight.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unyieldingmight.models.Gender
import com.example.unyieldingmight.services.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class DoneViewModel : ViewModel() {

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> = _registrationResult

    fun register(
        fName: String?, lName: String?, email: String?, passwordHash: String,
        street: String?, city: String?, region: String?, postcode: String?, country: String?,
        height: Float, weight: Float, multiplier: Float, membershipId: Int?,
        DOB: String, genderStr: String
    ) {
        viewModelScope.launch {
            try {
                val gender = try { 
                    Gender.valueOf(genderStr) 
                } catch (e: Exception) { 
                    Log.e("DoneViewModel", "Gender parsing error: ${e.message}")
                    Gender.OTHER 
                }
                val success = withContext(Dispatchers.IO) {
                    Database.registerCustomer(
                        fName, lName, email, passwordHash,
                        street, city, region, postcode, country,
                        height, weight, multiplier, membershipId,
                        Date.valueOf(DOB), gender
                    )
                }
                _registrationResult.value = success
            } catch (e: Exception) {
                Log.e("DoneViewModel", "Registration error: ${e.message}")
                _registrationResult.value = false
            }
        }
    }
}

