package com.example.tailtrail.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tailtrail.data.model.AddWalkRequest
import com.example.tailtrail.data.model.RoutePoint
import com.example.tailtrail.data.model.Walk
import com.example.tailtrail.data.repository.WalkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalkViewModel(private val walkRepository: WalkRepository) : ViewModel() {
    
    companion object {
        fun provideFactory(walkRepository: WalkRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WalkViewModel(walkRepository) as T
                }
            }
        }
    }
    
    private val _walks = MutableStateFlow<List<Walk>>(emptyList())
    val walks: StateFlow<List<Walk>> = _walks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _addWalkSuccess = MutableStateFlow<String?>(null)
    val addWalkSuccess: StateFlow<String?> = _addWalkSuccess.asStateFlow()
    
    fun loadUserWalks(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            walkRepository.getUserWalks(userId)
                .onSuccess { walksList ->
                    _walks.value = walksList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load walks"
                }
            
            _isLoading.value = false
        }
    }
    
    fun addWalk(userId: Int, genre: String, route: List<RoutePoint>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _addWalkSuccess.value = null
            
            val request = AddWalkRequest(
                userId = userId,
                genre = genre,
                stopDist = 100, // Default value for now
                noOfStops = route.size,
                route = route
            )
            
            walkRepository.addWalk(request)
                .onSuccess { message ->
                    _addWalkSuccess.value = message
                    // Reload walks after successful addition
                    loadUserWalks(userId)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to add walk"
                }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccess() {
        _addWalkSuccess.value = null
    }
} 