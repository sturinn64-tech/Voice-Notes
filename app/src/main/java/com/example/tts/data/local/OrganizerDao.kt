package com.example.tts.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizerDao {

    @Query(
        """
        SELECT * FROM folders
        WHERE userId = :userId
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeFolders(userId: String): Flow<List<FolderEntity>>

    @Query(
        """
        SELECT * FROM tags
        WHERE userId = :userId
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeTags(userId: String): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)

    @Query(
        """
        UPDATE audio_messages
        SET folderId = NULL
        WHERE folderId = :folderId
        """
    )
    suspend fun clearFolderFromMessages(folderId: Long)

    @Transaction
    suspend fun deleteFolderSafely(folderId: Long) {
        clearFolderFromMessages(folderId)
        deleteFolder(folderId)
    }

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToMessage(ref: AudioMessageTagCrossRef)

    @Query(
        """
        DELETE FROM audio_message_tag_refs
        WHERE messageId = :messageId AND tagId = :tagId
        """
    )
    suspend fun removeTagFromMessage(
        messageId: Long,
        tagId: Long
    )
}