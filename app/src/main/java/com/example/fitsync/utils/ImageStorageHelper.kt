package com.example.fitsync.data.local

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): String? {
        return try {
            // Create a unique file name
            val file = File(context.cacheDir, "meal_photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            // Compress and save the bitmap (80% quality is good for saving space)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()

            // Return the absolute path as a String
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}