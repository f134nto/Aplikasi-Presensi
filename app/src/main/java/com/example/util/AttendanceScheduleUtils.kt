package com.example.util

import java.util.Calendar

data class ScheduleCheckResult(
    val isValidWindow: Boolean,
    val isLate: Boolean,
    val statusLabel: String, // "TEPAT WAKTU", "TERLAMBAT", or "DILUAR JADWAL"
    val scheduleText: String,
    val message: String
)

object AttendanceScheduleUtils {

    fun getScheduleInfoText(type: String, date: Calendar = Calendar.getInstance()): String {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        return when (type) {
            "BERANGKAT" -> "☀️ Berangkat: 05.30 - 07.30 WIB (Tepat waktu s.d. 07.00 WIB, >07.00 TERLAMBAT)"
            "PULANG" -> {
                val windowStr = when (dayOfWeek) {
                    Calendar.FRIDAY -> "11.10 - 17.30 WIB (Hari Jumat)"
                    Calendar.SATURDAY -> "11.15 - 17.30 WIB (Hari Sabtu)"
                    else -> "13.35 - 17.30 WIB (Senin - Kamis)"
                }
                "🌙 Pulang: $windowStr"
            }
            "IZIN" -> "📄 Permohonan Izin: Dapat diajukan dari luar area GPS. Tidak perlu selfie foto. Wajib pilih Kategori & isi Keterangan."
            "SAKIT" -> "🏥 Permohonan Sakit: Dapat diajukan dari luar area GPS. Tidak perlu selfie foto. Wajib isi Keterangan Sakit."
            else -> "Presensi"
        }
    }

    fun checkSchedule(type: String, date: Calendar = Calendar.getInstance()): ScheduleCheckResult {
        if (type == "IZIN" || type == "SAKIT") {
            return ScheduleCheckResult(
                isValidWindow = true,
                isLate = false,
                statusLabel = type,
                scheduleText = "Pengajuan Permohonan $type",
                message = "Permohonan $type Aktif"
            )
        }

        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK) // SUNDAY = 1, MONDAY = 2, ..., SATURDAY = 7
        val hour = date.get(Calendar.HOUR_OF_DAY)
        val minute = date.get(Calendar.MINUTE)
        val currentMinutes = hour * 60 + minute

        if (type == "BERANGKAT") {
            val startMin = 5 * 60 + 30 // 05:30 = 330
            val lateMin = 7 * 60 + 0   // 07:00 = 420
            val endMin = 7 * 60 + 30   // 07:30 = 450

            return when {
                currentMinutes in startMin..lateMin -> {
                    ScheduleCheckResult(
                        isValidWindow = true,
                        isLate = false,
                        statusLabel = "TEPAT WAKTU",
                        scheduleText = "05.30 - 07.00 WIB",
                        message = "Presensi Berangkat Tepat Waktu"
                    )
                }
                currentMinutes in (lateMin + 1)..endMin -> {
                    ScheduleCheckResult(
                        isValidWindow = true,
                        isLate = true,
                        statusLabel = "TERLAMBAT",
                        scheduleText = "07.01 - 07.30 WIB",
                        message = "Presensi Berangkat Terlambat (Batas max tepat waktu 07.00 WIB)"
                    )
                }
                else -> {
                    ScheduleCheckResult(
                        isValidWindow = false,
                        isLate = false,
                        statusLabel = "DILUAR JADWAL",
                        scheduleText = "05.30 - 07.30 WIB",
                        message = "Jadwal presensi berangkat hanya dibuka pukul 05.30 - 07.30 WIB"
                    )
                }
            }
        } else {
            // PULANG
            val (startMin, dayName) = when (dayOfWeek) {
                Calendar.FRIDAY -> (11 * 60 + 10) to "Jumat (11.10 - 17.30 WIB)"
                Calendar.SATURDAY -> (11 * 60 + 15) to "Sabtu (11.15 - 17.30 WIB)"
                else -> (13 * 60 + 35) to "Senin-Kamis (13.35 - 17.30 WIB)"
            }
            val endMin = 17 * 60 + 30 // 17:30 = 1050

            return if (currentMinutes in startMin..endMin) {
                ScheduleCheckResult(
                    isValidWindow = true,
                    isLate = false,
                    statusLabel = "TEPAT WAKTU",
                    scheduleText = dayName,
                    message = "Presensi Pulang Sesuai Jadwal Hari Ini"
                )
            } else {
                ScheduleCheckResult(
                    isValidWindow = false,
                    isLate = false,
                    statusLabel = "DILUAR JADWAL",
                    scheduleText = dayName,
                    message = "Jadwal presensi pulang $dayName"
                )
            }
        }
    }
}
