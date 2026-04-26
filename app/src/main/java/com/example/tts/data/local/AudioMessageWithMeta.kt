package com.example.tts.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.tts.data.model.AudioMessage

data class AudioMessageWithMeta(
    @Embedded
    val message: AudioMessageEntity,

    @Relation(
        parentColumn = "folderId",
        entityColumn = "id"
    )
    val folder: FolderEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = AudioMessageTagCrossRef::class,
            parentColumn = "messageId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
) {
    fun toDomain(): AudioMessage {
        return message.toDomain(
            folder = folder,
            tags = tags
        )
    }
}