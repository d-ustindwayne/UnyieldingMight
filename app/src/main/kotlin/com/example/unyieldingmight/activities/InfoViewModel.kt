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

class InfoViewModel : ViewModel() {

    private val _bookingState = MutableLiveData<BookingState>()
    val bookingState: LiveData<BookingState> = _bookingState

    sealed class BookingState {
        object Idle : BookingState()
        object Loading : BookingState()
        object Success : BookingState()
        data class Error(val message: String) : BookingState()
        object AlreadyBooked : BookingState()
    }

    fun bookClass(classId: Int) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            
            try {
                val result = withContext(Dispatchers.IO) {
                    if (Database.isAlreadyBooked(classId)) {
                        return@withContext BookingState.AlreadyBooked
                    }
                    if (Database.bookClass(classId)) {
                        BookingState.Success
                    } else {
                        BookingState.Error("Booking failed. Class might be full.")
                    }
                }
                _bookingState.value = result
            } catch (e: Exception) {
                _bookingState.value = BookingState.Error("Database error occurred: ${e.message}")
            }
        }
    }

    fun getCurrentUserRole(): User.Role? {
        return Database.getCurrentUser()?.userClass
    }
}

