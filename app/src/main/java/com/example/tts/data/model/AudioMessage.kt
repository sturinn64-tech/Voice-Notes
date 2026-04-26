package com.example.tts.data.model

data class AudioMessage(
    val id: Long = 0L,
    val userId: String = "",
    val title: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val transcript: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val transcriptionStatus: TranscriptionStatus = TranscriptionStatus.COMPLETED,
    val transcriptionError: String? = null,

    val folder: Folder? = null,
    val tags: List<Tag> = emptyList(),

    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)