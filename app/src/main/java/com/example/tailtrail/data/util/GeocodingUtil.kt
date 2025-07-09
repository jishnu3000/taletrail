package com.example.tailtrail.data.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object GeocodingUtil {
    
    suspend fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use the new async API for Android 13+
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.let { address ->
                    return@withContext buildAddressString(address)
                }
            } else {
                // Use the old API for older Android versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.let { address ->
                    return@withContext buildAddressString(address)
                }
            }
            
            // Fallback to coordinates if geocoding fails
            "Location: $latitude, $longitude"
        } catch (e: Exception) {
            // Fallback to coordinates if geocoding fails
            "Location: $latitude, $longitude"
        }
    }
    
    private fun buildAddressString(address: android.location.Address): String {
        val parts = mutableListOf<String>()
        
        // Add feature name (like mall name, building name)
        address.featureName?.let { 
            if (it != address.locality && it != address.adminArea && it != address.countryName) {
                parts.add(it)
            }
        }
        
        // Add sub-locality (area/neighborhood)
        address.subLocality?.let { parts.add(it) }
        
        // Add street address
        address.thoroughfare?.let { parts.add(it) }
        address.subThoroughfare?.let { parts.add(it) }
        
        // Add locality (city) only if we don't have more specific info
        if (parts.isEmpty()) {
            address.locality?.let { parts.add(it) }
        }
        
        // Add admin area (state) only if we don't have more specific info
        if (parts.size <= 1) {
            address.adminArea?.let { parts.add(it) }
        }
        
        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            "Location: ${address.latitude}, ${address.longitude}"
        }
    }
} 