package com.example.unyieldingmight.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unyieldingmight.services.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp

class EditClassViewModel : ViewModel() {

    private val _editState = MutableLiveData<EditState>()
    val editState: LiveData<EditState> = _editState

    sealed class EditState {
        object Loading : EditState()
        object Success : EditState()
        data class Error(val message: String) : EditState()
    }

    fun updateClass(
        id: Int, title: String, description: String, 
        start: Timestamp, end: Timestamp, maxCap: Int, avgCal: Float
    ) {
        viewModelScope.launch {
            _editState.value = EditState.Loading
            
            try {
                val success = withContext(Dispatchers.IO) {
                    Database.editClass(id, title, description, start, end, maxCap, avgCal)
                }
                
                if (success) {
                    _editState.value = EditState.Success
                } else {
                    _editState.value = EditState.Error("Failed to update class")
                }
            } catch (e: Exception) {
                _editState.value = EditState.Error("Database error occurred: ${e.message}")
            }
        }
    }
}

