package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.PresensiEntity
import com.example.data.TeacherData
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

object ExportUtils {

    fun generatePdfReport(context: Context, records: List<PresensiEntity>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()

        // Header - MTs Ma'arif NU 1 Wangon
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 16f
        titlePaint.color = Color.parseColor("#047857") // Emerald Green
        canvas.drawText("REKAPITULASI PRESENSI GURU & KARYAWAN", 30f, 40f, titlePaint)

        titlePaint.textSize = 13f
        titlePaint.color = Color.DKGRAY
        canvas.drawText(TeacherData.SCHOOL_NAME + " - Wangon", 30f, 60f, titlePaint)

        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Area GPS: ${TeacherData.TARGET_LAT}, ${TeacherData.TARGET_LNG} (Max Radius 100m)", 30f, 75f, paint)
        canvas.drawText("Total Laporan: ${records.size} presensi", 30f, 90f, paint)

        // Divider
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, 100f, 565f, 100f, paint)

        // Table Header
        var y = 120f
        val headerPaint = Paint()
        headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        headerPaint.textSize = 10f
        headerPaint.color = Color.BLACK

        canvas.drawText("No", 25f, y, headerPaint)
        canvas.drawText("Waktu", 48f, y, headerPaint)
        canvas.drawText("Nama Pegawai", 130f, y, headerPaint)
        canvas.drawText("Tipe", 290f, y, headerPaint)
        canvas.drawText("Ket. Waktu", 345f, y, headerPaint)
        canvas.drawText("Status GPS", 425f, y, headerPaint)
        canvas.drawText("Laporan Harian", 495f, y, headerPaint)

        y += 10f
        canvas.drawLine(25f, y, 570f, y, paint)
        y += 15f

        val itemPaint = Paint()
        itemPaint.textSize = 8.5f
        itemPaint.color = Color.DKGRAY

        records.take(28).forEachIndexed { index, record ->
            val num = (index + 1).toString()
            val dt = "${record.dateString} ${record.timeString.take(5)}"
            val name = if (record.teacherName.length > 22) record.teacherName.substring(0, 20) + ".." else record.teacherName
            val type = record.type
            val ketWaktu = record.attendanceStatus
            val status = if (record.isInRadius) "Radius OK" else "Luar Area"
            val report = if (record.dailyReport.length > 12) record.dailyReport.substring(0, 10) + ".." else record.dailyReport

            canvas.drawText(num, 25f, y, itemPaint)
            canvas.drawText(dt, 48f, y, itemPaint)
            canvas.drawText(name, 130f, y, itemPaint)
            canvas.drawText(type, 290f, y, itemPaint)
            canvas.drawText(ketWaktu, 345f, y, itemPaint)
            canvas.drawText(status, 425f, y, itemPaint)
            canvas.drawText(report, 495f, y, itemPaint)

            y += 18f
            if (y > 780f) return@forEachIndexed
        }

        // Footer
        paint.textSize = 8f
        paint.color = Color.GRAY
        canvas.drawText("Dokumen ini dihasilkan otomatis oleh Sistem Presensi MTs Ma'arif NU 1 Wangon (E2EE Encrypted)", 30f, 810f, paint)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "Laporan_Presensi_MTs_Wangon.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(outputFile))
            pdfDocument.close()
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    fun generateCsvReport(context: Context, records: List<PresensiEntity>): File? {
        val csvFile = File(context.cacheDir, "Laporan_Presensi_MTs_Wangon.csv")
        try {
            val writer = FileWriter(csvFile)
            // UTF-8 BOM for Excel compatibility
            writer.write("\uFEFF")
            writer.write("ID,Nama Lengkap,NIP,Peran,Tipe Presensi,Ket Waktu,Tanggal,Waktu,Latitude,Longitude,Jarak (m),Status Radius,Laporan Harian,Status Enkripsi,Sinkron Awan\n")

            records.forEach { r ->
                val escapedReport = r.dailyReport.replace("\"", "\"\"")
                val radiusText = if (r.isInRadius) "VALID (Dalam Radius 100m)" else "LUAR AREA (>100m)"
                val syncText = if (r.isSynced) "TERKIRIM" else "PENDING OFFLINE"

                writer.write("${r.id},\"${r.teacherName}\",\"${r.nip}\",\"${r.role}\",${r.type},${r.attendanceStatus},${r.dateString},${r.timeString},${r.latitude},${r.longitude},${"%.1f".format(r.distanceMeters)},$radiusText,\"$escapedReport\",${r.encryptionStatus},$syncText\n")
            }

            writer.flush()
            writer.close()
            return csvFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
