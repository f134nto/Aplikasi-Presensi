package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PresensiRepository(private val presensiDao: PresensiDao) {
    val allPresensi: Flow<List<PresensiEntity>> = presensiDao.getAllPresensi()

    fun getTodayCount(dateStr: String): Flow<Int> = presensiDao.getTodayCount(dateStr)
    fun getTodayBerangkatCount(dateStr: String): Flow<Int> = presensiDao.getTodayBerangkatCount(dateStr)
    fun getTodayPulangCount(dateStr: String): Flow<Int> = presensiDao.getTodayPulangCount(dateStr)

    suspend fun insertPresensi(presensi: PresensiEntity): Long {
        return presensiDao.insertPresensi(presensi)
    }

    suspend fun getUnsyncedCount(): Int {
        return presensiDao.getUnsyncedPresensi().size
    }

    suspend fun syncAll(): Int {
        val unsynced = presensiDao.getUnsyncedPresensi()
        presensiDao.markAllSynced()
        return unsynced.size
    }

    suspend fun deletePresensi(id: Long) {
        presensiDao.deletePresensi(id)
    }

    suspend fun populateInitialDataIfEmpty() {
        // Will be called on startup to seed realistic attendance records if empty
    }
}
