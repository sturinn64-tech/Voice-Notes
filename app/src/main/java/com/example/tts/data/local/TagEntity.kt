package com.example.tts.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tts.data.model.Tag

@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        userId = userId,
        name = name,
        createdAt = createdAt
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        id = id,
        userId = userId,
        name = name,
        createdAt = createdAt
    )
}