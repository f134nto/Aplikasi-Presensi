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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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

    var showTeacherPicker by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var teacherSearchQuery by remember { mutableStateOf("") }
    var currentTimeString by remember { mutableStateOf("") }
    var overrideSchedule by remember { mutableStateOf(false) }

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

        // Segmented Control: BERANGKAT, PULANG, IZIN, SAKIT
        Text(
            text = "Jenis Presensi / Permohonan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
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
                    text = "📍 Diluar Radius Area Sekolah: Tombol Berangkat & Pulang disembunyikan. Hanya tombol Izin dan Sakit yang dapat dipilih.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF92400E),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isInRadius) {
                val isBerangkat = presensiType == "BERANGKAT"
                Button(
                    onClick = { viewModel.setPresensiType("BERANGKAT") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBerangkat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isBerangkat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("☀️ Berangkat", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                val isPulang = presensiType == "PULANG"
                Button(
                    onClick = { viewModel.setPresensiType("PULANG") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPulang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isPulang) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("🌙 Pulang", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            val isIzin = presensiType == "IZIN"
            Button(
                onClick = { viewModel.setPresensiType("IZIN") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIzin) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isIzin) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Text("📄 Izin", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            val isSakit = presensiType == "SAKIT"
            Button(
                onClick = { viewModel.setPresensiType("SAKIT") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSakit) Color(0xFFDC2626) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSakit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Text("🏥 Sakit", fontWeight = FontWeight.Bold, fontSize = 11.sp)
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

        // GPS Geofencing Card
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
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            tint = if (isInRadius) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Area GPS Sekolah (Max 100m)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { viewModel.updateLocation(TeacherData.TARGET_LAT, TeacherData.TARGET_LNG) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Radius Status Badge
                val badgeColor = if (isInRadius) Color(0xFF059669) else Color(0xFFEF4444)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isInRadius) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "Status",
                            tint = badgeColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isInRadius) "DALAM RADIUS AREAL SEKOLAH" else "DI LUAR RADIUS AREAL SEKOLAH",
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Jarak dari Titik Sekolah: ${LocationUtils.formatDistance(distanceMeters)} (Target: -7.504599, 109.062339)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Simulation Location
                OutlinedTextField(
                    value = "Lat: %.6f, Lng: %.6f".format(currentLat, currentLng),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Koordinat GPS Anda Saat Ini") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera / Selfie Card (Only for BERANGKAT and PULANG)
        if (presensiType == "BERANGKAT" || presensiType == "PULANG") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Foto Selfie Kehadiran",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (selfieBase64 == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    val mockBitmap = createSelfieWatermarkBitmap(selectedTeacher.name)
                                    viewModel.setSelfiePhoto(mockBitmap)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Take Photo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ketuk untuk Ambil Foto Selfie",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Lengkap watermark lokasi GPS & waktu",
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
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Captured",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Foto Selfie Berhasil Diambil",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Watermark E2EE & GPS Tersimpan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.clearSelfiePhoto() }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Ulangi")
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
            // Laporan Harian Text Box (Berangkat / Pulang)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Laporan Harian (Aktivitas / Kegiatan Pekerjaan)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = dailyReportText,
                        onValueChange = { viewModel.setDailyReportText(it) },
                        placeholder = { Text("Contoh: Mengajar Mapel Matematika Kelas IX B, rekap modul, dan pembimbingan ekstrakurikuler...") },
                        minLines = 3,
                        maxLines = 5,
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
