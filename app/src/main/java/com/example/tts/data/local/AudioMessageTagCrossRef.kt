package com.example.tts.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "audio_message_tag_refs",
    primaryKeys = ["messageId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = AudioMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["tagId"])
    ]
)
data class AudioMessageTagCrossRef(
    val messageId: Long,
    val tagId: Long
)