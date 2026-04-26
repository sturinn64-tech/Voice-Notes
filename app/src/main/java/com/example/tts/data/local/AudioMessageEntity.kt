package com.example.tts.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.model.TranscriptionStatus

@Entity(
    tableName = "audio_messages",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "createdAt"]),
        Index(value = ["userId", "isDeleted"]),
        Index(value = ["userId", "folderId"])
    ]
)
data class AudioMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val userId: String,
    val title: String,
    val fileName: String,
    val filePath: String,
    val transcript: String,
    val createdAt: Long,
    val durationMs: Long,
    val isFavorite: Boolean,
    val transcriptionStatus: String,
    val transcriptionError: String?,

    val folderId: Long? = null,

    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

fun AudioMessageEntity.toDomain(
    folder: FolderEntity? = null,
    tags: List<TagEntity> = emptyList()
): AudioMessage {
    return AudioMessage(
        id = id,
        userId = userId,
        title = title,
        fileName = fileName,
        filePath = filePath,
        transcript = transcript,
        createdAt = createdAt,
        durationMs = durationMs,
        isFavorite = isFavorite,
        transcriptionStatus = runCatching {
            TranscriptionStatus.valueOf(transcriptionStatus)
        }.getOrDefault(TranscriptionStatus.COMPLETED),
        transcriptionError = transcriptionError,
        folder = folder?.toDomain(),
        tags = tags.map { it.toDomain() },
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
}

fun AudioMessage.toEntity(): AudioMessageEntity {
    return AudioMessageEntity(
        id = id,
        userId = userId,
        title = title,
        fileName = fileName,
        filePath = filePath,
        transcript = transcript,
        createdAt = createdAt,
        durationMs = durationMs,
        isFavorite = isFavorite,
        transcriptionStatus = transcriptionStatus.name,
        transcriptionError = transcriptionError,
        folderId = folder?.id,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
}