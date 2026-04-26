package com.example.tts.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tts.data.model.Folder

@Entity(
    tableName = "folders",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "name"], unique = true)
    ]
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        userId = userId,
        name = name,
        createdAt = createdAt
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        userId = userId,
        name = name,
        createdAt = createdAt
    )
}