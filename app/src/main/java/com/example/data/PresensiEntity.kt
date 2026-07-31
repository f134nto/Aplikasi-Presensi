package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presensi_records")
data class PresensiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherName: String,
    val nip: String,
    val role: String,
    val type: String, // "BERANGKAT" or "PULANG"
    val dateString: String,
    val timeString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,
    val isInRadius: Boolean,
    val dailyReport: String,
    val attendanceStatus: String = "TEPAT WAKTU", // "TEPAT WAKTU", "TERLAMBAT", or "DILUAR JADWAL"
    val photoBase64: String = "",
    val isSynced: Boolean = false,
    val encryptionStatus: String = "AES-256 E2EE"
)
