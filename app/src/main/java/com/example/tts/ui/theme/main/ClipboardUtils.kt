package com.example.tts.ui.theme.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.example.tts.data.model.AudioMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun copyTranscriptToClipboard(
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

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(title, text)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "Текст скопирован", Toast.LENGTH_SHORT).show()
}