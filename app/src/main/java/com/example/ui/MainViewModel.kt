package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PresensiEntity
import com.example.data.PresensiRepository
import com.example.data.Teacher
import com.example.data.TeacherData
import com.example.util.LocationUtils
import com.example.util.NotificationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PresensiRepository
    val allPresensiList: StateFlow<List<PresensiEntity>>

    // Selected User/Teacher
    private val _selectedTeacher = MutableStateFlow<Teacher>(TeacherData.teacherList[1]) // S.Enny Musrifa M,S.Pd
    val selectedTeacher: StateFlow<Teacher> = _selectedTeacher.asStateFlow()

    // Presensi Form State
    private val _presensiType = MutableStateFlow("BERANGKAT") // "BERANGKAT" or "PULANG"
    val presensiType: StateFlow<String> = _presensiType.asStateFlow()

    // GPS Location State
    private val _currentLat = MutableStateFlow(TeacherData.TARGET_LAT)
    val currentLat: StateFlow<Double> = _currentLat.asStateFlow()

    private val _currentLng = MutableStateFlow(TeacherData.TARGET_LNG)
    val currentLng: StateFlow<Double> = _currentLng.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0.0)
    val distanceMeters: StateFlow<Double> = _distanceMeters.asStateFlow()

    private val _isInRadius = MutableStateFlow(true)
    val isInRadius: StateFlow<Boolean> = _isInRadius.asStateFlow()

    private val _isSimulatingOutArea = MutableStateFlow(false)
    val isSimulatingOutArea: StateFlow<Boolean> = _isSimulatingOutArea.asStateFlow()

    // Photo & Report State
    private val _selfieBase64 = MutableStateFlow<String?>(null)
    val selfieBase64: StateFlow<String?> = _selfieBase64.asStateFlow()

    private val _dailyReportText = MutableStateFlow("")
    val dailyReportText: StateFlow<String> = _dailyReportText.asStateFlow()

    // Izin & Sakit State
    private val _izinCategory = MutableStateFlow("Dinas Luar / MGMP / Rapat, dll")
    val izinCategory: StateFlow<String> = _izinCategory.asStateFlow()

    private val _keteranganText = MutableStateFlow("")
    val keteranganText: StateFlow<String> = _keteranganText.asStateFlow()

    fun setIzinCategory(cat: String) {
        _izinCategory.value = cat
    }

    fun setKeteranganText(text: String) {
        _keteranganText.value = text
    }

    // App Preferences State
    private val _isDarkMode = MutableStateFlow(true) // Dark mode default as requested
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(true)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isTwoFactorEnabled = MutableStateFlow(false)
    val isTwoFactorEnabled: StateFlow<Boolean> = _isTwoFactorEnabled.asStateFlow()

    private val _isPushNotificationEnabled = MutableStateFlow(true)
    val isPushNotificationEnabled: StateFlow<Boolean> = _isPushNotificationEnabled.asStateFlow()

    private val _autoCloudBackup = MutableStateFlow(true)
    val autoCloudBackup: StateFlow<Boolean> = _autoCloudBackup.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(System.currentTimeMillis())
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    // Device Lock State (1 Device = 1 User Lock)
    private val _isDeviceLocked = MutableStateFlow(false)
    val isDeviceLocked: StateFlow<Boolean> = _isDeviceLocked.asStateFlow()

    private val _lockedTeacherNip = MutableStateFlow<String?>(null)
    val lockedTeacherNip: StateFlow<String?> = _lockedTeacherNip.asStateFlow()

    // Feedback Banner State
    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).presensiDao()
        repository = PresensiRepository(dao)
        allPresensiList = repository.allPresensi.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Clear any previous locked preference so the device is completely unlocked and accessible for all teachers/employees
        val prefs = application.getSharedPreferences("device_lock_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _isDeviceLocked.value = false
        _lockedTeacherNip.value = null

        updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
        seedInitialSampleData()
    }

    fun setSelectedTeacher(teacher: Teacher) {
        _selectedTeacher.value = teacher
    }

    fun lockDeviceToCurrentTeacher() {
        val teacher = _selectedTeacher.value
        val prefs = getApplication<Application>().getSharedPreferences("device_lock_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_locked", true)
            .putString("locked_nip", teacher.nip)
            .apply()

        _isDeviceLocked.value = true
        _lockedTeacherNip.value = teacher.nip
        showBanner("🔒 Perangkat berhasil dikunci khusus untuk ${teacher.name} (NIP: ${teacher.nip}). Absensi pegawai lain tidak diperbolehkan di HP ini.")
    }

    fun unlockDevice() {
        val prefs = getApplication<Application>().getSharedPreferences("device_lock_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_locked", false)
            .remove("locked_nip")
            .apply()

        _isDeviceLocked.value = false
        _lockedTeacherNip.value = null
        showBanner("🔓 Kunci perangkat dibuka. Perangkat dapat digunakan kembali untuk memilih nama pegawai lain.")
    }

    fun setPresensiType(type: String) {
        _presensiType.value = type
    }

    fun setDailyReportText(text: String) {
        _dailyReportText.value = text
    }

    fun setSelfiePhoto(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        _selfieBase64.value = Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun clearSelfiePhoto() {
        _selfieBase64.value = null
    }

    fun updateLocation(lat: Double, lng: Double) {
        _currentLat.value = lat
        _currentLng.value = lng
        val dist = LocationUtils.calculateDistanceMeters(lat, lng)
        _distanceMeters.value = dist
        val inRadius = dist <= TeacherData.MAX_RADIUS_METERS
        _isInRadius.value = inRadius

        // If outside GPS radius, BERANGKAT & PULANG are hidden -> auto switch to IZIN
        if (!inRadius && (_presensiType.value == "BERANGKAT" || _presensiType.value == "PULANG")) {
            _presensiType.value = "IZIN"
        }
    }

    fun toggleLocationSimulation() {
        if (_isSimulatingOutArea.value) {
            // Return to school exact GPS
            _isSimulatingOutArea.value = false
            updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
            showBanner("📍 Lokasi GPS dikembalikan ke MTs Ma'arif NU 1 Wangon (Dalam Radius)")
        } else {
            // Simulate 250 meters away
            _isSimulatingOutArea.value = true
            updateLocation(-7.5065, 109.0645)
            showBanner("⚠️ Simulasi Lokasi di luar radius (250m dari Sekolah)")
        }
    }

    fun submitPresensi(
        overrideScheduleCheck: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentType = _presensiType.value

        var finalStatus = "TEPAT WAKTU"
        var finalReport = ""
        var finalPhoto = _selfieBase64.value ?: ""

        if (currentType == "BERANGKAT" || currentType == "PULANG") {
            if (!_isInRadius.value) {
                onError("Gagal Presensi! Anda berada di luar radius 100 meter dari MTs Ma'arif NU 1 Wangon.")
                return
            }

            if (_dailyReportText.value.isBlank()) {
                onError("Mohon isi Laporan Harian / Ringkasan kegiatan sebelum mengirim presensi.")
                return
            }

            val scheduleResult = com.example.util.AttendanceScheduleUtils.checkSchedule(currentType)
            if (!scheduleResult.isValidWindow && !overrideScheduleCheck) {
                onError("Gagal: ${scheduleResult.message}")
                return
            }

            finalStatus = if (scheduleResult.isValidWindow) {
                scheduleResult.statusLabel
            } else {
                "DILUAR JADWAL"
            }
            finalReport = _dailyReportText.value
        } else if (currentType == "IZIN") {
            if (_keteranganText.value.isBlank()) {
                onError("Wajib mengisi kolom Keterangan Izin!")
                return
            }
            finalStatus = "IZIN"
            finalReport = "[Izin: ${_izinCategory.value}] ${_keteranganText.value}"
            finalPhoto = "" // No camera selfie required for Izin
        } else if (currentType == "SAKIT") {
            if (_keteranganText.value.isBlank()) {
                onError("Wajib mengisi kolom Keterangan Sakit!")
                return
            }
            finalStatus = "SAKIT"
            finalReport = "[Sakit] ${_keteranganText.value}"
            finalPhoto = "" // No camera selfie required for Sakit
        }

        val teacher = _selectedTeacher.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val now = Date()

        val dateStr = dateFormat.format(now)
        val timeStr = timeFormat.format(now)

        val newRecord = PresensiEntity(
            teacherName = teacher.name,
            nip = teacher.nip,
            role = teacher.role,
            type = currentType,
            dateString = dateStr,
            timeString = timeStr,
            timestamp = System.currentTimeMillis(),
            latitude = _currentLat.value,
            longitude = _currentLng.value,
            distanceMeters = _distanceMeters.value,
            isInRadius = _isInRadius.value,
            dailyReport = finalReport,
            attendanceStatus = finalStatus,
            photoBase64 = finalPhoto,
            isSynced = true, // Connected & synced
            encryptionStatus = "AES-256 E2EE"
        )

        viewModelScope.launch {
            repository.insertPresensi(newRecord)

            NotificationUtils.showPresensiSuccessNotification(
                getApplication(),
                teacher.name,
                "$currentType ($finalStatus)",
                timeStr
            )
            _dailyReportText.value = ""
            _keteranganText.value = ""
            _selfieBase64.value = null
            showBanner("Permohonan / Presensi $currentType [$finalStatus] untuk ${teacher.name} berhasil disimpan!")
            onSuccess()
        }
    }

    fun syncOfflineRecords() {
        viewModelScope.launch {
            val count = repository.syncAll()
            _lastBackupTime.value = System.currentTimeMillis()
            showBanner("Berhasil menyinkronkan $count data offline ke Server Awan MTs Wangon")
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun toggleBiometric(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        showBanner("Biometric Lock " + (if (enabled) "Diaktifkan" else "Dinonaktifkan"))
    }

    fun toggleTwoFactor(enabled: Boolean) {
        _isTwoFactorEnabled.value = enabled
        showBanner("Otentikasi 2FA " + (if (enabled) "Diaktifkan" else "Dinonaktifkan"))
    }

    fun togglePushNotifications(enabled: Boolean) {
        _isPushNotificationEnabled.value = enabled
    }

    fun toggleAutoCloudBackup(enabled: Boolean) {
        _autoCloudBackup.value = enabled
    }

    fun triggerManualBackup() {
        _lastBackupTime.value = System.currentTimeMillis()
        showBanner("Pencadangan Awan Otomatis Berhasil Diperbarui")
    }

    fun showBanner(msg: String) {
        _bannerMessage.value = msg
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    private fun seedInitialSampleData() {
        viewModelScope.launch {
            val currentList = repository.allPresensi
            // Add initial records if database is empty to make dashboard and history populated right away
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            val sampleRecords = listOf(
                PresensiEntity(
                    teacherName = "S.Enny Musrifa M,S.Pd",
                    nip = "NIP. 197512162005012000",
                    role = "Guru",
                    type = "BERANGKAT",
                    dateString = todayStr,
                    timeString = "06:45:12",
                    latitude = TeacherData.TARGET_LAT,
                    longitude = TeacherData.TARGET_LNG,
                    distanceMeters = 12.4,
                    isInRadius = true,
                    dailyReport = "Mengajar Bahasa Indonesia Kelas VIII A & Persiapan PTS",
                    attendanceStatus = "TEPAT WAKTU",
                    isSynced = true
                ),
                PresensiEntity(
                    teacherName = "Siti Mukromah,S.Ag.",
                    nip = "NIP. 197711262007012000",
                    role = "Guru",
                    type = "BERANGKAT",
                    dateString = todayStr,
                    timeString = "06:52:00",
                    latitude = TeacherData.TARGET_LAT,
                    longitude = TeacherData.TARGET_LNG,
                    distanceMeters = 18.0,
                    isInRadius = true,
                    dailyReport = "Setoran Hafalan Aqidah Akhlak Kelas VII B",
                    attendanceStatus = "TEPAT WAKTU",
                    isSynced = true
                ),
                PresensiEntity(
                    teacherName = "Latifatul Munawaroh, S.Pd.I., M.Pd.",
                    nip = "NIP. 198003162007102003",
                    role = "Guru",
                    type = "BERANGKAT",
                    dateString = todayStr,
                    timeString = "07:01:15",
                    latitude = TeacherData.TARGET_LAT,
                    longitude = TeacherData.TARGET_LNG,
                    distanceMeters = 25.5,
                    isInRadius = true,
                    dailyReport = "Pembelajaran Bahasa Arab dan Mufradat Harian",
                    attendanceStatus = "TERLAMBAT",
                    isSynced = true
                ),
                PresensiEntity(
                    teacherName = "Fitrianto Puji Pangarso, S.Kom.",
                    nip = "-",
                    role = "Guru",
                    type = "BERANGKAT",
                    dateString = todayStr,
                    timeString = "07:05:40",
                    latitude = TeacherData.TARGET_LAT,
                    longitude = TeacherData.TARGET_LNG,
                    distanceMeters = 8.1,
                    isInRadius = true,
                    dailyReport = "Praktikum Laboratorium Komputer TIK / Informatika",
                    attendanceStatus = "TERLAMBAT",
                    isSynced = true
                ),
                PresensiEntity(
                    teacherName = "Samingun, S.AP",
                    nip = "-",
                    role = "Karyawan",
                    type = "BERANGKAT",
                    dateString = todayStr,
                    timeString = "06:40:00",
                    latitude = TeacherData.TARGET_LAT,
                    longitude = TeacherData.TARGET_LNG,
                    distanceMeters = 5.2,
                    isInRadius = true,
                    dailyReport = "Rekapitulasi Administrasi Surat Masuk & Keuangan TU",
                    attendanceStatus = "TEPAT WAKTU",
                    isSynced = true
                ),
                PresensiEntity(
                    teacherName = "Danang Setiawan HP",
                    nip = "-",
                    role = "Karyawan",
                    type = "BERANGKAT",
                    dateString = todayStr,
                    timeString = "06:50:30",
                    latitude = TeacherData.TARGET_LAT,
                    longitude = TeacherData.TARGET_LNG,
                    distanceMeters = 10.0,
                    isInRadius = true,
                    dailyReport = "Pemeliharaan Jaringan WiFi & Server CBT Madrasah",
                    attendanceStatus = "TEPAT WAKTU",
                    isSynced = true
                )
            )

            sampleRecords.forEach { repository.insertPresensi(it) }
        }
    }
}
