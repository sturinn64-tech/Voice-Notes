package com.example.tts.data.model

import com.google.firebase.Timestamp

data class AudioMessage(
    val id: String = "",
    val userId: String = "",
    val fileName: String = "",
    val transcript: String = "",
    val timestamp: Timestamp = Timestamp.now()
)