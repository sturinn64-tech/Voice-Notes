package com.example.tts.ui.theme.main

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.tts.data.model.AudioMessage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun shareTranscript(
    context: Context,
    message: AudioMessage
) {
    val title = message.title.ifBlank {
        message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
    }

    val dateText = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        .format(Date(message.createdAt))

    val text = buildString {
        appendLine("Название: $title")
        appendLine("Дата: $dateText")
        appendLine("Файл: ${message.fileName}")
        appendLine()
        appendLine("Текст заметки:")
        appendLine(message.transcript.ifBlank { "Текст отсутствует" })
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val chooser = Intent.createChooser(intent, "Поделиться текстом")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

fun shareAudioFile(
    context: Context,
    message: AudioMessage
) {
    val file = File(message.filePath)
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val mimeType = when (file.extension.lowercase(Locale.getDefault())) {
        "wav" -> "audio/wav"
        "mp3" -> "audio/mpeg"
        "m4a" -> "audio/mp4"
        "aac" -> "audio/aac"
        "ogg" -> "audio/ogg"
        else -> "audio/*"
    }

    val title = message.title.ifBlank {
        message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "Аудиозаметка: $title")
        clipData = ClipData.newRawUri(title, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val chooser = Intent.createChooser(intent,"Поделиться аудио")
    context.startActivity(chooser)
}
