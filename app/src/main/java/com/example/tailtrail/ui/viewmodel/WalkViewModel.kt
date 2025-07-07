package com.example.tailtrail.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailtrail.data.api.WalkRequest
import com.example.tailtrail.data.api.WalkResponse
import com.example.tailtrail.data.repository.WalkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WalkViewModel : ViewModel() {
    private val repository = WalkRepository()
    private val _walks = MutableStateFlow<List<WalkResponse>>(emptyList())
    val walks: StateFlow<List<WalkResponse>> = _walks

    private val _addWalkResult = MutableStateFlow<String?>(null)
    val addWalkResult: StateFlow<String?> = _addWalkResult

    fun fetchWalks(userId: Int) {
        viewModelScope.launch {
            val response = repository.getUserWalks(userId)
            if (response.isSuccessful) {
                _walks.value = response.body() ?: emptyList()
            }
        }
    }

    fun addWalk(walkRequest: WalkRequest) {
        viewModelScope.launch {
            val response = repository.addWalk(walkRequest)
            _addWalkResult.value = if (response.isSuccessful) {
                response.body()?.message
            } else {
                "Failed to add walk"
            }
        }
    }
} 