package com.example.tailtrail.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tailtrail.data.model.AddWalkRequest
import com.example.tailtrail.data.model.RoutePoint
import com.example.tailtrail.data.model.Walk
import com.example.tailtrail.data.model.WalkDetails
import com.example.tailtrail.data.model.CheckInResponse
import com.example.tailtrail.data.repository.WalkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
    
    // Walk Details State
    private val _walkDetails = MutableStateFlow<WalkDetails?>(null)
    val walkDetails: StateFlow<WalkDetails?> = _walkDetails.asStateFlow()
    
    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()
    
    private val _detailsError = MutableStateFlow<String?>(null)
    val detailsError: StateFlow<String?> = _detailsError.asStateFlow()
    
        // Check-in State
    private val _isCheckingIn = MutableStateFlow<Int?>(null) // routeId that is being checked in
    val isCheckingIn: StateFlow<Int?> = _isCheckingIn.asStateFlow()

    private val _checkInError = MutableStateFlow<String?>(null)
    val checkInError: StateFlow<String?> = _checkInError.asStateFlow()

    private val _checkInSuccess = MutableStateFlow<String?>(null)
    val checkInSuccess: StateFlow<String?> = _checkInSuccess.asStateFlow()

    // Story visibility state
    private val _showStoryForRouteId = MutableStateFlow<Int?>(null)
    val showStoryForRouteId: StateFlow<Int?> = _showStoryForRouteId.asStateFlow()

    fun setShowStoryForRoute(routeId: Int?) {
        _showStoryForRouteId.value = routeId
    }
    
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
    
    fun addWalk(userId: Int, genre: String, route: List<RoutePoint>, stopDist: Int, userStopsCount: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _addWalkSuccess.value = null
            
            val request = AddWalkRequest(
                userId = userId,
                genre = genre,
                stopDist = stopDist,
                noOfStops = userStopsCount, // Only count user-selected stops, not current location
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
    
    fun loadWalkDetails(walkId: Int) {
        viewModelScope.launch {
            try {
                println("WalkViewModel: Starting loadWalkDetails for walkId=$walkId")
                System.out.println("=== WalkViewModel: Starting loadWalkDetails for walkId=$walkId ===")
                
                _isLoadingDetails.value = true
                _detailsError.value = null
                _walkDetails.value = null
                
                // Validate walkId
                if (walkId <= 0) {
                    println("WalkViewModel: Invalid walkId=$walkId")
                    System.out.println("=== WalkViewModel: Invalid walkId=$walkId ===")
                    _detailsError.value = "Invalid walk ID"
                    _isLoadingDetails.value = false
                    return@launch
                }
                
                println("WalkViewModel: Calling repository getWalkDetails")
                System.out.println("=== WalkViewModel: Calling repository getWalkDetails ===")
                
                walkRepository.getWalkDetails(walkId)
                    .onSuccess { details ->
                        println("WalkViewModel: Walk details loaded successfully: $details")
                        System.out.println("=== WalkViewModel: Walk details loaded successfully: $details ===")
                        _walkDetails.value = details
                    }
                    .onFailure { exception ->
                        println("WalkViewModel: Failed to load walk details: ${exception.message}")
                        System.out.println("=== WalkViewModel: Failed to load walk details: ${exception.message} ===")
                        _detailsError.value = exception.message ?: "Failed to load walk details"
                    }
            } catch (e: Exception) {
                println("WalkViewModel: Exception in loadWalkDetails: ${e.message}")
                System.out.println("=== WalkViewModel: Exception in loadWalkDetails: ${e.message} ===")
                e.printStackTrace()
                _detailsError.value = "Unexpected error: ${e.message}"
            } finally {
                println("WalkViewModel: Setting isLoadingDetails to false")
                System.out.println("=== WalkViewModel: Setting isLoadingDetails to false ===")
                _isLoadingDetails.value = false
            }
        }
    }
    
    fun checkIn(
        userId: Int,
        walkId: Int,
        routeId: Int,
        userLatitude: Double,
        userLongitude: Double
    ) {
        println("WalkViewModel: Starting check-in for routeId=$routeId")
        
        viewModelScope.launch {
            _isCheckingIn.value = routeId
            clearCheckInError()
            clearCheckInSuccess()
            
            try {
                val result = walkRepository.checkIn(
                    userLat = userLatitude,
                    userLng = userLongitude,
                    routeId = routeId
                )
                
                result.onSuccess { response ->
                    val message = response.message ?: "Unknown response"
                    println("WalkViewModel: Check-in response: $message")
                    
                    // Simple success check - if message contains "successful" or "within range"
                    if (message.contains("successful", ignoreCase = true) || 
                        message.contains("within range", ignoreCase = true)) {
                        _checkInSuccess.value = message
                    } else {
                        _checkInError.value = message
                    }
                }.onFailure { exception ->
                    println("WalkViewModel: Check-in failed: ${exception.message}")
                    _checkInError.value = exception.message ?: "Check-in failed"
                }
            } catch (e: Exception) {
                println("WalkViewModel: Exception during check-in: ${e.message}")
                _checkInError.value = "Error: ${e.message}"
            } finally {
                _isCheckingIn.value = null
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccess() {
        _addWalkSuccess.value = null
    }
    
    fun clearDetailsError() {
        _detailsError.value = null
    }
    
    fun clearWalkDetails() {
        _walkDetails.value = null
    }
    
    fun clearCheckInError() {
        _checkInError.value = null
    }
    
    fun clearCheckInSuccess() {
        _checkInSuccess.value = null
    }
    
    fun setCheckInError(error: String) {
        _checkInError.value = error
    }
} 