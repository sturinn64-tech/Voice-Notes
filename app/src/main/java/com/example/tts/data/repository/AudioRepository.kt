package com.example.tts.data.repository

import com.example.tts.data.local.AudioMessageDao
import com.example.tts.data.local.toDomain
import com.example.tts.data.local.toEntity
import com.example.tts.data.model.AudioMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AudioRepository(
    private val audioMessageDao: AudioMessageDao
) {
    fun observeAudioMessagesForUser(userId: String): Flow<List<AudioMessage>> {
        return audioMessageDao.observeMessagesForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveAudioMessage(message: AudioMessage): Long {
        return audioMessageDao.insert(message.toEntity())
    }

    suspend fun deleteAudioMessage(message: AudioMessage) {
        if (message.id != 0L) {
            audioMessageDao.deleteById(message.id)
        }
    }

    suspend fun setFavorite(messageId: Long, isFavorite: Boolean) {
        audioMessageDao.updateFavorite(messageId, isFavorite)
    }

    suspend fun updateAudioMessage(
        messageId: Long,
        title: String,
        transcript: String
    ) {
        audioMessageDao.updateMessage(
            id = messageId,
            title = title,
            transcript = transcript
        )
    }
}