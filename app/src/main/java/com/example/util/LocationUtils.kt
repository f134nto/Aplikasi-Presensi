package com.example.util

import android.location.Location
import com.example.data.TeacherData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {

    /**
     * Calculates distance in meters between current location and school target coordinates
     * Target MTs Ma'arif NU 1 Wangon: -7.50459941616919, 109.06233985805635
     */
    fun calculateDistanceMeters(
        currentLat: Double,
        currentLng: Double,
        targetLat: Double = TeacherData.TARGET_LAT,
        targetLng: Double = TeacherData.TARGET_LNG
    ): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(targetLat - currentLat)
        val dLng = Math.toRadians(targetLng - currentLng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(currentLat)) * cos(Math.toRadians(targetLat)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }

    fun isWithinSchoolRadius(
        currentLat: Double,
        currentLng: Double,
        maxRadiusMeters: Double = TeacherData.MAX_RADIUS_METERS
    ): Boolean {
        val distance = calculateDistanceMeters(currentLat, currentLng)
        return distance <= maxRadiusMeters
    }

    fun formatDistance(distanceMeters: Double): String {
        return if (distanceMeters < 1000) {
            "%.1f m".format(distanceMeters)
        } else {
            "%.2f km".format(distanceMeters / 1000.0)
        }
    }
}
