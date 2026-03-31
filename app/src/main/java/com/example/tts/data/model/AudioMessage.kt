package com.example.tts.data.model

import com.google.firebase.Timestamp

data class AudioMessage(
    val id: Long = 0L,
    val userId: String = "",
    val title: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val transcript: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false
)