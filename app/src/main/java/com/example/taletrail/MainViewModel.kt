// MainViewModel.kt
package com.example.taletrail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taletrail.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _response = MutableStateFlow("Click the button to fetch data")
    val response: StateFlow<String> = _response

//    fun fetchData() {
//        viewModelScope.launch {
//            try {
//                val result = RetrofitClient.api.checkLocation(
//                    userLat = 12.93104898404996,
//                    userLng = 77.60786751826028,
//                    checkpointLat = 12.931025280664695,
//                    checkpointLng = 77.60769164975153
//                )
//                Log.d("API_RESPONSE", "Message: ${result.message}")
//                _response.value = result.message
//            } catch (e: Exception) {
//                Log.e("API_ERROR", "Exception: ${e.message}", e)
//                _response.value = "Error: ${e.message}"
//            }
//        }
//    }

    fun fetchData(userLat: Double, userLng: Double) {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.api.checkLocation(
                    userLat = userLat,
                    userLng = userLng,
                    checkpointLat = 12.931025280664695,
                    checkpointLng = 77.60769164975153
                )
                Log.d("API_RESPONSE", "Message: ${result.message}")
                _response.value = result.message
            } catch (e: Exception) {
                Log.e("API_ERROR", "Exception: ${e.message}", e)
                _response.value = "Error: ${e.message}"
            }
        }
    }

}