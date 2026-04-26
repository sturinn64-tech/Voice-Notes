package com.example.tts.data.model

data class Folder(
    val id: Long = 0L,
    val userId: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
)