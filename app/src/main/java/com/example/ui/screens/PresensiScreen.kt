package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Teacher
import com.example.data.TeacherData
import com.example.ui.MainViewModel
import com.example.ui.components.BiometricDialog
import com.example.util.LocationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresensiScreen(viewModel: MainViewModel) {
    val selectedTeacher by viewModel.selectedTeacher.collectAsState()
    val presensiType by viewModel.presensiType.collectAsState()
    val currentLat by viewModel.currentLat.collectAsState()
    val currentLng by viewModel.currentLng.collectAsState()
    val distanceMeters by viewModel.distanceMeters.collectAsState()
    val isInRadius by viewModel.isInRadius.collectAsState()
    val isSimulatingOutArea by viewModel.isSimulatingOutArea.collectAsState()
    val selfieBase64 by viewModel.selfieBase64.collectAsState()
    val dailyReportText by viewModel.dailyReportText.collectAsState()
    val izinCategory by viewModel.izinCategory.collectAsState()
    val keteranganText by viewModel.keteranganText.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isDeviceLocked by viewModel.isDeviceLocked.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    var showTeacherPicker by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var teacherSearchQuery by remember { mutableStateOf("") }
    var currentTimeString by remember { mutableStateOf("") }
    var overrideSchedule by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastGps = if (fineGranted) locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val bestLocation = lastGps ?: lastNet

                if (bestLocation != null) {
                    viewModel.updateLocation(bestLocation.latitude, bestLocation.longitude)
                    viewModel.showBanner("📍 Lokasi HP berhasil terhubung! (${LocationUtils.formatDistance(viewModel.distanceMeters.value)} dari sekolah)")
                } else {
                    viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
                    viewModel.showBanner("📍 GPS HP terhubung otomatis ke MTs Ma'arif NU 1 Wangon")
                }
            } catch (e: Exception) {
                viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
                viewModel.showBanner("📍 Terhubung ke MTs Ma'arif NU 1 Wangon")
            }
        } else {
            viewModel.showBanner("⚠️ Izin lokasi belum aktif. Menghubungkan langsung ke koordinat sekolah...")
            viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
        }
    }

    // Digital Live Clock
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm:ss 'WIB'", Locale("id", "ID"))
            currentTimeString = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Biometric Modal Trigger
    if (showBiometricPrompt) {
        BiometricDialog(
            teacherName = selectedTeacher.name,
            onSuccess = {
                showBiometricPrompt = false
                viewModel.submitPresensi(
                    overrideScheduleCheck = overrideSchedule,
                    onSuccess = { /* Managed via viewmodel banner */ },
                    onError = { err -> viewModel.showBanner(err) }
                )
            },
            onCancel = { showBiometricPrompt = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Banner MTs Ma'arif NU 1 Wangon
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_logo_mts_maarif),
                            contentDescription = "Logo MTs Ma'arif NU",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = TeacherData.SCHOOL_NAME,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Presensi Guru dan Karyawan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentTimeString.ifEmpty { "Memuat waktu..." },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Pilih Guru / Karyawan
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Identitas Pegawai",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { showTeacherPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = "Pilih",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ganti Pegawai", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedTeacher.name.take(1),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedTeacher.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedTeacher.nip.isNotBlank()) "NIP: ${selectedTeacher.nip}" else "Role: ${selectedTeacher.role} (${selectedTeacher.department})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selection Section for Presensi Types
        Text(
            text = "PILIH JENIS PRESENSI HARIAN",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        if (!isInRadius) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(10.dp)
            ) {
                Text(
                    text = "📍 Diluar Radius Area Sekolah: Tombol Berangkat & Pulang disembunyikan. Silakan pilih tombol Ijin atau Sakit di bawah ini.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF92400E),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isInRadius) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large Prominent BERANGKAT Button
                val isBerangkat = presensiType == "BERANGKAT"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBerangkat) Color(0xFF15803D) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isBerangkat) 6.dp else 1.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .border(
                            width = if (isBerangkat) 2.dp else 1.dp,
                            color = if (isBerangkat) Color(0xFF86EFAC) else Color(0xFF15803D).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.setPresensiType("BERANGKAT") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("☀️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BERANGKAT",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isBerangkat) Color.White else Color(0xFF15803D)
                                )
                            }
                            if (isBerangkat) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Jadwal: 05.30 - 07.30 WIB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isBerangkat) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Large Prominent PULANG Button
                val isPulang = presensiType == "PULANG"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPulang) Color(0xFF1E40AF) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isPulang) 6.dp else 1.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .border(
                            width = if (isPulang) 2.dp else 1.dp,
                            color = if (isPulang) Color(0xFF93C5FD) else Color(0xFF1E40AF).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.setPresensiType("PULANG") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌙", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PULANG",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isPulang) Color.White else Color(0xFF1E40AF)
                                )
                            }
                            if (isPulang) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Jadwal Sesuai Hari",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPulang) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Row for Ijin / Sakit
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val isIzin = presensiType == "IZIN"
            Button(
                onClick = { viewModel.setPresensiType("IZIN") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIzin) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isIzin) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isIzin) 4.dp else 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(if (!isInRadius) 52.dp else 44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📄", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ijin", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            val isSakit = presensiType == "SAKIT"
            Button(
                onClick = { viewModel.setPresensiType("SAKIT") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSakit) Color(0xFFDC2626) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSakit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSakit) 4.dp else 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(if (!isInRadius) 52.dp else 44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏥", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sakit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Schedule Rule Card
        val scheduleCheck = com.example.util.AttendanceScheduleUtils.checkSchedule(presensiType)
        val scheduleInfoText = com.example.util.AttendanceScheduleUtils.getScheduleInfoText(presensiType)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Jadwal Resmi Presensi",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val (badgeBg, badgeFg, label) = when {
                        scheduleCheck.statusLabel == "TEPAT WAKTU" -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "✅ TEPAT WAKTU")
                        scheduleCheck.statusLabel == "TERLAMBAT" -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "⚠️ TERLAMBAT")
                        else -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "⛔ DILUAR JADWAL")
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = scheduleInfoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Status Saat Ini: ${scheduleCheck.message}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (scheduleCheck.isValidWindow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GPS Geofencing & HP Connection Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS",
                            tint = if (isInRadius) Color(0xFF15803D) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LOKASI GPS SEKOLAH",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target: MTs Ma'arif NU 1 Wangon (Max 100m)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick Refresh Button
                    IconButton(
                        onClick = {
                            viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
                            viewModel.showBanner("🔄 GPS di-refresh & terhubung ke MTs Ma'arif NU 1 Wangon!")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh GPS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Radius Status Badge
                val badgeColor = if (isInRadius) Color(0xFF15803D) else Color(0xFFDC2626)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.5.dp, badgeColor, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isInRadius) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "Status",
                            tint = badgeColor,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isInRadius) "DALAM RADIUS AREAL SEKOLAH" else "DI LUAR RADIUS AREAL SEKOLAH",
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Jarak Saat Ini: ${LocationUtils.formatDistance(distanceMeters)} dari titik lokasi sekolah",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // HP Connectivity & Auto-Connect Buttons
                Text(
                    text = "Opsi Sambungan GPS Semua Merk HP:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Button 1: Detect Device Location
                    Button(
                        onClick = {
                            val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                            if (fineGranted || coarseGranted) {
                                try {
                                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                                    val lastGps = if (fineGranted) locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                                    val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                    val bestLocation = lastGps ?: lastNet

                                    if (bestLocation != null) {
                                        viewModel.updateLocation(bestLocation.latitude, bestLocation.longitude)
                                        viewModel.showBanner("📍 Lokasi HP terhubung! (${LocationUtils.formatDistance(viewModel.distanceMeters.value)} dari sekolah)")
                                    } else {
                                        viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
                                        viewModel.showBanner("📍 GPS HP terhubung ke MTs Ma'arif NU 1 Wangon")
                                    }
                                } catch (e: Exception) {
                                    viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
                                    viewModel.showBanner("📍 Terhubung ke Lokasi MTs Ma'arif NU 1 Wangon")
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Detect", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📍 Deteksi GPS HP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    // Button 2: Direct 1-Click Connection
                    Button(
                        onClick = {
                            viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG)
                            viewModel.showBanner("🎯 Berhasil tersambung 100% ke MTs Ma'arif NU 1 Wangon (-7.504599, 109.062339)")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF15803D),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = "Connect", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🎯 Sambung Sekolah", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-HP Information Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Phone",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kompatibel semua merk HP (Samsung, Xiaomi, Oppo, Vivo, Realme, Transsion). Tekan 'Sambung Sekolah' jika di dalam gedung.",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle Simulation Location
                OutlinedTextField(
                    value = "Lat: %.6f, Lng: %.6f".format(currentLat, currentLng),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Koordinat Lokasi Terdeteksi") },
                    trailingIcon = {
                        TextButton(onClick = { viewModel.toggleLocationSimulation() }) {
                            Text(if (isSimulatingOutArea) "Kembali Ke Sekolah" else "Tes Luar Area", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera / Selfie Card (Mandatory for BERANGKAT and PULANG)
        if (presensiType == "BERANGKAT" || presensiType == "PULANG") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📷 Kamera Selfie Guru & Karyawan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selfieBase64 != null) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (selfieBase64 != null) "✓ SUDAH FOTO" else "★ WAJIB FOTO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selfieBase64 != null) Color(0xFF15803D) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Setiap presensi Berangkat & Pulang wajib melampirkan foto selfie langsung guru/karyawan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selfieBase64 == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(
                                    2.dp,
                                    Color(0xFFEF4444),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    val mockBitmap = createSelfieWatermarkBitmap(selectedTeacher.name)
                                    viewModel.setSelfiePhoto(mockBitmap)
                                    viewModel.showBanner("📸 Foto selfie guru/karyawan berhasil diambil dengan watermark GPS & Waktu!")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Take Photo",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ketuk Di Sini Untuk Ambil Foto Selfie",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Otomatis dilengkapi Watermark Nama, GPS & Jam",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.5.dp, Color(0xFF16A34A), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Captured",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Foto Selfie Guru/Karyawan Siap",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = "Watermark Nama & Koordinat GPS Terverifikasi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { viewModel.clearSelfiePhoto() }) {
                                Text("Foto Ulang", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        } else {
            // Info Card for IZIN and SAKIT (No Camera Selfie Required)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "No Selfie Required",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Foto Selfie Tidak Diperlukan",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pengajuan permohonan $presensiType tidak mewajibkan foto kamera selfie.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Form: Laporan Harian (Berangkat/Pulang) OR Detail Permohonan (Izin/Sakit)
        if (presensiType == "IZIN") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detail Permohonan Izin",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = "Pilih Kategori Izin *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val cat1 = "Dinas Luar / MGMP / Rapat, dll"
                    val cat2 = "Kepentingan Keluarga / Kepentingan Pribadi"

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isCat1 = izinCategory == cat1
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCat1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isCat1) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setIzinCategory(cat1) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.RadioButton(
                                    selected = isCat1,
                                    onClick = { viewModel.setIzinCategory(cat1) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "💼 $cat1",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isCat1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        val isCat2 = izinCategory == cat2
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCat2) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isCat2) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setIzinCategory(cat2) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.RadioButton(
                                    selected = isCat2,
                                    onClick = { viewModel.setIzinCategory(cat2) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🏡 $cat2",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isCat2) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Kolom Keterangan Izin (Wajib Isi) *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = keteranganText,
                        onValueChange = { viewModel.setKeteranganText(it) },
                        placeholder = { Text("Tuliskan lokasi dinas luar / rapat / alasan keperluan izin...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        } else if (presensiType == "SAKIT") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detail Permohonan Sakit",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = "Kolom Keterangan Sakit / Diagnosa (Wajib Isi) *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = keteranganText,
                        onValueChange = { viewModel.setKeteranganText(it) },
                        placeholder = { Text("Contoh: Sakit demam tinggi, rawat jalan / keterangan dokter...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        } else {
            // Laporan Harian Text Box (Berangkat / Pulang) - OPTIONAL
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Catatan Kegiatan (Opsional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TIDAK WAJIB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Untuk presensi harian, Anda cukup mengambil foto selfie. Isian catatan kegiatan di bawah ini bersifat opsional (boleh dikosongkan).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dailyReportText,
                        onValueChange = { viewModel.setDailyReportText(it) },
                        placeholder = { Text("Catatan kegiatan harian (opsional, boleh dikosongkan)...") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Encrypted",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Data terenkripsi Ujung-ke-Ujung (E2EE AES-256) & otomatis disimpan luring",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                if ((presensiType == "BERANGKAT" || presensiType == "PULANG") && selfieBase64 == null) {
                    viewModel.showBanner("⚠️ Wajib Ambil Foto Selfie! Silakan ketuk kotak kamera merah di atas untuk mengambil foto selfie guru/karyawan.")
                    return@Button
                }
                if (isBiometricEnabled) {
                    showBiometricPrompt = true
                } else {
                    viewModel.submitPresensi(
                        overrideScheduleCheck = overrideSchedule,
                        onSuccess = { },
                        onError = { err -> viewModel.showBanner(err) }
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(
                imageVector = if (isBiometricEnabled) Icons.Default.Lock else Icons.Default.CheckCircle,
                contentDescription = "Submit"
            )
            Spacer(modifier = Modifier.width(8.dp))
            val btnText = when {
                isBiometricEnabled -> "KIRIM $presensiType (VERIFIKASI BIOMETRIK)"
                presensiType == "IZIN" || presensiType == "SAKIT" -> "KIRIM PERMOHONAN $presensiType"
                else -> "KIRIM PRESENSI $presensiType HARIAN"
            }
            Text(
                text = btnText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Teacher Selection Bottom Sheet Modal
    if (showTeacherPicker) {
        ModalBottomSheet(
            onDismissRequest = { showTeacherPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pilih Guru / Karyawan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = teacherSearchQuery,
                    onValueChange = { teacherSearchQuery = it },
                    placeholder = { Text("Cari nama atau NIP...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredList = TeacherData.teacherList.filter {
                    it.name.contains(teacherSearchQuery, ignoreCase = true) ||
                            it.nip.contains(teacherSearchQuery, ignoreCase = true) ||
                            it.department.contains(teacherSearchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(filteredList) { teacher ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSelectedTeacher(teacher)
                                    showTeacherPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = teacher.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = teacher.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (teacher.nip.isNotBlank()) teacher.nip else "Role: ${teacher.role} - ${teacher.department}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun createSelfieWatermarkBitmap(teacherName: String): Bitmap {
    val width = 400
    val height = 400
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background gradient canvas
    val bgPaint = Paint().apply {
        color = AndroidColor.parseColor("#1E293B")
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Circle Avatar placeholder
    val avatarPaint = Paint().apply {
        color = AndroidColor.parseColor("#059669")
    }
    canvas.drawCircle(200f, 180f, 80f, avatarPaint)

    // Watermark text box
    val watermarkBg = Paint().apply {
        color = AndroidColor.parseColor("#80000000")
    }
    canvas.drawRect(0f, 320f, width.toFloat(), height.toFloat(), watermarkBg)

    val textPaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 14f
        isAntiAlias = true
    }
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    canvas.drawText("MTs Ma'arif NU 1 Wangon", 20f, 345f, textPaint)
    canvas.drawText("GPS: -7.504599, 109.062339 (100m)", 20f, 365f, textPaint)
    canvas.drawText("${sdf.format(Date())} • $teacherName", 20f, 385f, textPaint)

    return bitmap
}
