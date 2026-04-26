package com.example.tts.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioMessageDao {

    @Transaction
    @Query(
        """
        SELECT * FROM audio_messages
        WHERE userId = :userId
        AND isDeleted = 0
        ORDER BY isFavorite DESC, createdAt DESC
        """
    )
    fun observeMessagesForUser(userId: String): Flow<List<AudioMessageWithMeta>>

    @Transaction
    @Query(
        """
        SELECT * FROM audio_messages
        WHERE userId = :userId
        AND isDeleted = 1
        ORDER BY deletedAt DESC, createdAt DESC
        """
    )
    fun observeDeletedMessagesForUser(userId: String): Flow<List<AudioMessageWithMeta>>

    @Query("SELECT * FROM audio_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): AudioMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AudioMessageEntity): Long

    @Query("DELETE FROM audio_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        DELETE FROM audio_messages
        WHERE userId = :userId
        AND isDeleted = 1
        """
    )
    suspend fun deleteAllDeletedForUser(userId: String)

    @Query(
        """
        UPDATE audio_messages
        SET isFavorite = :isFavorite
        WHERE id = :id
        """
    )
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query(
        """
        UPDATE audio_messages
        SET title = :title,
            transcript = :transcript
        WHERE id = :id
        """
    )
    suspend fun updateMessage(id: Long, title: String, transcript: String)

    @Query(
        """
        UPDATE audio_messages
        SET transcript = :transcript,
            transcriptionStatus = :status,
            transcriptionError = :error
        WHERE id = :id
        """
    )
    suspend fun updateTranscriptionState(
        id: Long,
        transcript: String,
        status: String,
        error: String?
    )

    @Query(
        """
        UPDATE audio_messages
        SET folderId = :folderId
        WHERE id = :messageId
        """
    )
    suspend fun updateFolderId(messageId: Long, folderId: Long?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Query(
        """
        SELECT * FROM folders
        WHERE userId = :userId
        AND name = :name
        LIMIT 1
        """
    )
    suspend fun getFolderByName(userId: String, name: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query(
        """
        SELECT * FROM tags
        WHERE userId = :userId
        AND name = :name
        LIMIT 1
        """
    )
    suspend fun getTagByName(userId: String, name: String): TagEntity?

    @Query(
        """
        DELETE FROM audio_message_tag_refs
        WHERE messageId = :messageId
        """
    )
    suspend fun deleteTagRefsForMessage(messageId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagRef(ref: AudioMessageTagCrossRef)

    @Query(
        """
        UPDATE audio_messages
        SET isDeleted = 1,
            deletedAt = :deletedAt
        WHERE id = :id
        """
    )
    suspend fun moveToTrash(id: Long, deletedAt: Long)

    @Query(
        """
        UPDATE audio_messages
        SET isDeleted = 0,
            deletedAt = NULL
        WHERE id = :id
        """
    )
    suspend fun restoreFromTrash(id: Long)
}