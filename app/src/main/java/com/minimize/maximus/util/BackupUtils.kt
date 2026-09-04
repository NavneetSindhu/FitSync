package com.minimize.maximus.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.WorkoutSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupUtils {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun renderDrawableToBitmap(
        context: Context,
        drawableRes: Int,
        targetWidth: Int,
        targetHeight: Int,
        tintColor: Int? = null
    ): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)?.mutate() ?: return null
        if (tintColor != null) {
            drawable.setTint(tintColor)
        }
        val scale = 3 // 3x supersampling for crisp high-DPI PDF output
        val bitmap = Bitmap.createBitmap(targetWidth * scale, targetHeight * scale, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Serializes all workout sessions into formatted JSON string.
     */
    fun exportToJson(workouts: List<WorkoutSession>): String {
        return json.encodeToString(workouts)
    }

    /**
     * Deserializes JSON string back into list of workout sessions with new 0-IDs for clean Room autoincrement insertion.
     */
    fun importFromJson(jsonString: String): List<WorkoutSession> {
        val parsed = json.decodeFromString<List<WorkoutSession>>(jsonString)
        return parsed.map { session ->
            session.copy(id = 0L)
        }
    }

    /**
     * Reads JSON content directly from a file Uri selected by the user.
     */
    suspend fun importFromJsonUri(context: Context, uri: Uri): List<WorkoutSession> = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open selected file")
        val content = inputStream.bufferedReader().use { it.readText() }
        importFromJson(content.trim())
    }

    /**
     * Formats all workout sessions into standard spreadsheet CSV string.
     */
    fun exportToCsv(workouts: List<WorkoutSession>): String {
        val builder = StringBuilder()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // CSV Header
        builder.append("Session Date,Workout Name,Exercise,Set #,Set Type,Weight,Reps,Completed\n")

        workouts.sortedBy { it.date }.forEach { session ->
            val formattedDate = dateFormatter.format(Date(session.date))
            val sessionNameEscaped = "\"${session.name.replace("\"", "\"\"")}\""

            session.exercise.forEach { exercise ->
                val exerciseNameEscaped = "\"${exercise.name.replace("\"", "\"\"")}\""
                exercise.sets.forEach { set ->
                    builder.append(
                        listOf(
                            formattedDate,
                            sessionNameEscaped,
                            exerciseNameEscaped,
                            set.setNumber,
                            set.setType,
                            set.weight,
                            set.reps,
                            if (set.isCompleted) "YES" else "NO"
                        ).joinToString(",")
                    ).append("\n")
                }
            }
        }

        return builder.toString()
    }

    /**
     * Generates a real .csv file in cache and returns its sharable FileProvider Uri.
     */
    suspend fun exportCsvFile(context: Context, workouts: List<WorkoutSession>): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "maximus_workouts_${System.currentTimeMillis()}.csv")
        file.writeText(exportToCsv(workouts))

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Generates a real .json database backup file in cache and returns its sharable FileProvider Uri.
     */
    suspend fun exportJsonFile(context: Context, workouts: List<WorkoutSession>): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "maximus_backup_${System.currentTimeMillis()}.json")
        file.writeText(exportToJson(workouts))

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Generates a formatted multi-page PDF workout report with summaries and tables.
     */
    suspend fun exportPdfReport(
        context: Context,
        workouts: List<WorkoutSession>,
        userName: String = "Athlete",
        weightUnit: String = "kg"
    ): Uri = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#18181B")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#71717A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#18181B")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#27272A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.parseColor("#09090B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E4E4E7")
            strokeWidth = 1f
        }

        val bgCardPaint = Paint().apply {
            color = Color.parseColor("#F4F4F5")
        }

        // 1. Header Banner (Brand Logo + Title & Subtitle in a Row)
        val logoWidth = 68f
        val logoHeight = 22f
        val logoTop = 38f
        val logoLeft = 40f
        val textStartX = logoLeft + logoWidth + 14f

        val logoBitmap = renderDrawableToBitmap(
            context = context,
            drawableRes = R.drawable.app_logo,
            targetWidth = logoWidth.toInt(),
            targetHeight = logoHeight.toInt(),
            tintColor = Color.parseColor("#18181B")
        )

        if (logoBitmap != null) {
            val destRect = RectF(logoLeft, logoTop, logoLeft + logoWidth, logoTop + logoHeight)
            val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(logoBitmap, null, destRect, bitmapPaint)
        }

        canvas.drawText("Maximus Workout Logbook Report", textStartX, 48f, titlePaint)
        val reportDate = SimpleDateFormat("MMMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Athlete: $userName • Generated: $reportDate", textStartX, 62f, subtitlePaint)

        var y = 74f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // 2. Summary Metrics Strip
        val totalVolume = workouts.sumOf { s -> s.exercise.sumOf { e -> e.sets.sumOf { (it.weight * it.reps).toDouble() } } }.toInt()
        val totalSets = workouts.sumOf { s -> s.exercise.sumOf { it.sets.size } }

        canvas.drawRoundRect(40f, y, 555f, y + 54f, 12f, 12f, bgCardPaint)
        canvas.drawText("TOTAL WORKOUTS", 60f, y + 20f, subtitlePaint)
        canvas.drawText("${workouts.size}", 60f, y + 42f, boldBodyPaint)

        canvas.drawText("TOTAL VOLUME", 230f, y + 20f, subtitlePaint)
        canvas.drawText("$totalVolume $weightUnit", 230f, y + 42f, boldBodyPaint)

        canvas.drawText("TOTAL SETS", 420f, y + 20f, subtitlePaint)
        canvas.drawText("$totalSets sets", 420f, y + 42f, boldBodyPaint)
        y += 74f

        // 3. Recent Workout Sessions Listing
        canvas.drawText("RECENT TRAINING SESSIONS", 40f, y, sectionPaint)
        y += 16f

        val sessionDateFormatter = SimpleDateFormat("EEE, MMM dd • yyyy", Locale.getDefault())

        workouts.sortedByDescending { it.date }.take(8).forEach { session ->
            if (y > 780f) return@forEach // Keep within A4 bounds

            val dateStr = sessionDateFormatter.format(Date(session.date))
            val sessionVol = session.exercise.sumOf { e -> e.sets.sumOf { (it.weight * it.reps).toDouble() } }.toInt()
            val exSummary = session.exercise.joinToString(", ") { "${it.name} (${it.sets.size} sets)" }

            canvas.drawText("${session.name} ($dateStr)", 40f, y, boldBodyPaint)
            canvas.drawText("$sessionVol $weightUnit", 480f, y, boldBodyPaint)
            y += 14f

            canvas.drawText(exSummary.take(90) + if (exSummary.length > 90) "..." else "", 40f, y, subtitlePaint)
            y += 14f

            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 14f
        }

        // Footer Branding
        canvas.drawText("Generated with Maximus • Dare To Do More", 40f, 810f, subtitlePaint)

        pdfDocument.finishPage(page)

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val pdfFile = File(exportDir, "maximus_workout_report_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(pdfFile)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }
}
