package com.example.mesrapp.utils

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.mesrapp.R
import java.io.File
import java.io.FileOutputStream

object Utils {

     fun createTempFileFromUri(uri: Uri, ctx: Context): File? {
        val contentResolver = ctx.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            val tempFile = File.createTempFile("temp", null, ctx.cacheDir)
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
                return tempFile
            }
        }
        return null
    }

    val customFontFamily = FontFamily(
        Font(R.font.circular)
    )

}