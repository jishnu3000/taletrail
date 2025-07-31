package com.example.tailtrail.data.util

import kotlin.math.*

object LocationUtil {
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in meters
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000 // Earth's radius in meters
        
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return r * c
    }
    
    /**
     * Check if user is within range of a stop
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param stopLat Stop's latitude
     * @param stopLon Stop's longitude
     * @param range Range in meters (default 50 meters)
     * @return true if user is within range
     */
    fun isWithinRange(
        userLat: Double,
        userLon: Double,
        stopLat: Double,
        stopLon: Double,
        range: Double = 50.0
    ): Boolean {
        val distance = calculateDistance(userLat, userLon, stopLat, stopLon)
        return distance <= range
    }
} 