package com.minimize.maximus.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): String? {
        return try {
            val file = File(context.cacheDir, "meal_photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
