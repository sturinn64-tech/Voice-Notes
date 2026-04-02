package com.example.tts.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tts.data.local.AudioMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioMessageDao {

    @Query(
        """
        SELECT * FROM audio_messages
        WHERE userId = :userId
        ORDER BY isFavorite DESC, createdAt DESC
        """
    )
    fun observeMessagesForUser(userId: String): Flow<List<AudioMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AudioMessageEntity): Long

    @Query("DELETE FROM audio_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

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
}