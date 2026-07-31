package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TeacherData

@Composable
fun ApiDocScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Code, contentDescription = "API", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Dokumentasi REST API Integration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Spesifikasi API untuk integrasi sistem pihak ketiga ${TeacherData.SCHOOL_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Auth Header Box
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = "Key", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Otentikasi API Token", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Authorization: Bearer mts_wangon_sec_key_9921a48b\nContent-Type: application/json",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Endpoint 1: Submit Presensi
        Text("1. POST /api/v1/presensi/submit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .padding(12.dp)
        ) {
            Text(
                text = """
{
  "nip": "197512162005012000",
  "nama": "S.Enny Musrifa M,S.Pd",
  "tipe": "BERANGKAT",
  "latitude": -7.5045994,
  "longitude": 109.0623398,
  "jarak_meter": 12.4,
  "laporan_harian": "Mengajar Bahasa Indonesia",
  "e2ee_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4"
}
                """.trimIndent(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF34D399)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Endpoint 2: Rekapitulasi Bulanan
        Text("2. GET /api/v1/presensi/rekapitulasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .padding(12.dp)
        ) {
            Text(
                text = """
{
  "status": "success",
  "sekolah": "MTs Ma'arif NU 1 Wangon",
  "bulan": "Juli 2026",
  "total_presensi": 142,
  "tingkat_kehadiran": "96.5%"
}
                """.trimIndent(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF60A5FA)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tutup Spesifikasi API")
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
