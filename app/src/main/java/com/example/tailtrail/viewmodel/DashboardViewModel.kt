package com.example.tailtrail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailtrail.data.repository.WalkRepository
import com.example.tailtrail.data.model.DashboardStats
import com.example.tailtrail.data.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stats: DashboardStats? = null,
    val error: String? = null
)

class DashboardViewModel(
    private val walkRepository: WalkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Temporary storage for full-screen map navigation
    var tempVisitedPlaces: List<Place>? = null
        private set
    
    var tempNotVisitedPlaces: List<Place>? = null
        private set

    fun setTempVisitedPlaces(places: List<Place>) {
        tempVisitedPlaces = places
    }

    fun setTempNotVisitedPlaces(places: List<Place>) {
        tempNotVisitedPlaces = places
    }

    fun fetchDashboardStats(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            walkRepository.getDashboardStats(userId)
                .onSuccess { stats ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        stats = stats,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun formatDistance(distanceInMeters: Int): String {
        return if (distanceInMeters >= 1000) {
            String.format("%.2f km", distanceInMeters / 1000.0)
        } else {
            "$distanceInMeters m"
        }
    }

    fun getProgressPercentage(completed: Int, total: Int): Float {
        return if (total > 0) (completed.toFloat() / total.toFloat()) else 0f
    }
}
