package com.example.tts.ui.theme.main

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class ExportManager(
    private val context: Context
) {
    fun exportTextFile(
        displayName: String,
        content: String
    ): Uri {
        return writeToDownloads(
            displayName = displayName,
            mimeType = "text/plain"
        ) {
            output ->
            output.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    fun exportAudioFile(
        sourcePath: String,
        displayName: String,
        mimeType: String
    ): Uri {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            throw IOException("Аудиофайл не найден")
        }

        return writeToDownloads(
            displayName = displayName,
            mimeType = mimeType
        ) { output ->
            sourceFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }

    private fun writeToDownloads(
        displayName: String,
        mimeType: String,
        writer: (OutputStream) -> Unit
    ): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/VoiceNotes")
            }

            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val uri = resolver.insert(collection, values)
                ?: throw IOException("Не удалось создать файл в Downloads")

            try {
                resolver.openOutputStream(uri)?.use(writer)
                    ?: throw IOException("Не удалось открыть файл для записи")
                uri
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadsDir, "VoiceNotes")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val targetFile = File(targetDir, displayName)
            FileOutputStream(targetFile).use(writer)
            Uri.fromFile(targetFile)
        }
    }
}