package com.example.unyieldingmight.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unyieldingmight.models.MembershipResult
import com.example.unyieldingmight.services.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinkMembershipViewModel : ViewModel() {

    private val _validationResult = MutableLiveData<MembershipResult?>()
    val validationResult: LiveData<MembershipResult?> = _validationResult

    fun validate(membershipId: Int, email: String?) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    Database.validateMembership(membershipId, email)
                }
                _validationResult.value = result
            } catch (e: Exception) {
                Log.e("LinkMembershipVM", "Validation error: ${e.message}")
                _validationResult.value = null
            }
        }
    }
}

