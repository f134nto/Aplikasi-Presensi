package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresensiDao {
    @Query("SELECT * FROM presensi_records ORDER BY timestamp DESC")
    fun getAllPresensi(): Flow<List<PresensiEntity>>

    @Query("SELECT * FROM presensi_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedPresensi(): List<PresensiEntity>

    @Query("SELECT COUNT(*) FROM presensi_records WHERE dateString = :dateStr")
    fun getTodayCount(dateStr: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM presensi_records WHERE dateString = :dateStr AND type = 'BERANGKAT'")
    fun getTodayBerangkatCount(dateStr: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM presensi_records WHERE dateString = :dateStr AND type = 'PULANG'")
    fun getTodayPulangCount(dateStr: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresensi(presensi: PresensiEntity): Long

    @Update
    suspend fun updatePresensi(presensi: PresensiEntity)

    @Query("UPDATE presensi_records SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE presensi_records SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllSynced()

    @Query("DELETE FROM presensi_records WHERE id = :id")
    suspend fun deletePresensi(id: Long)

    @Query("DELETE FROM presensi_records")
    suspend fun deleteAll()
}
