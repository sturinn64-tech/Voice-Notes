package com.example.tts.data.repository

import com.example.tts.data.local.AudioMessageDao
import com.example.tts.data.local.AudioMessageTagCrossRef
import com.example.tts.data.local.FolderEntity
import com.example.tts.data.local.TagEntity
import com.example.tts.data.local.toDomain
import com.example.tts.data.local.toEntity
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.model.TranscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class AudioRepository(
    private val audioMessageDao: AudioMessageDao
) {

    fun observeAudioMessagesForUser(userId: String): Flow<List<AudioMessage>> {
        return audioMessageDao.observeMessagesForUser(userId).map { items ->
            items.map { it.toDomain() }
        }
    }

    fun observeDeletedAudioMessagesForUser(userId: String): Flow<List<AudioMessage>> {
        return audioMessageDao.observeDeletedMessagesForUser(userId).map { items ->
            items.map { it.toDomain() }
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

    suspend fun emptyTrash(userId: String) {
        audioMessageDao.deleteAllDeletedForUser(userId)
    }

    suspend fun moveToTrash(messageId: Long) {
        audioMessageDao.moveToTrash(
            id = messageId,
            deletedAt = System.currentTimeMillis()
        )
    }

    suspend fun restoreFromTrash(messageId: Long) {
        audioMessageDao.restoreFromTrash(messageId)
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

    suspend fun updateFolder(
        messageId: Long,
        folderName: String
    ) {
        val message = audioMessageDao.getMessageById(messageId) ?: return

        val safeFolderName = folderName.trim()

        if (safeFolderName.isBlank()) {
            audioMessageDao.updateFolderId(
                messageId = messageId,
                folderId = null
            )
            return
        }

        val existingFolder = audioMessageDao.getFolderByName(
            userId = message.userId,
            name = safeFolderName
        )

        val folderId = existingFolder?.id ?: run {
            val insertedId = audioMessageDao.insertFolder(
                FolderEntity(
                    userId = message.userId,
                    name = safeFolderName
                )
            )

            if (insertedId != -1L) {
                insertedId
            } else {
                audioMessageDao.getFolderByName(
                    userId = message.userId,
                    name = safeFolderName
                )?.id
            }
        }

        audioMessageDao.updateFolderId(
            messageId = messageId,
            folderId = folderId
        )
    }

    suspend fun updateTags(
        messageId: Long,
        tags: List<String>
    ) {
        val message = audioMessageDao.getMessageById(messageId) ?: return

        val safeTags = tags
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.getDefault()) }

        audioMessageDao.deleteTagRefsForMessage(messageId)

        safeTags.forEach { tagName ->
            val existingTag = audioMessageDao.getTagByName(
                userId = message.userId,
                name = tagName
            )

            val tagId = existingTag?.id ?: run {
                val insertedId = audioMessageDao.insertTag(
                    TagEntity(
                        userId = message.userId,
                        name = tagName
                    )
                )

                if (insertedId != -1L) {
                    insertedId
                } else {
                    audioMessageDao.getTagByName(
                        userId = message.userId,
                        name = tagName
                    )?.id
                }
            }

            if (tagId != null) {
                audioMessageDao.insertTagRef(
                    AudioMessageTagCrossRef(
                        messageId = messageId,
                        tagId = tagId
                    )
                )
            }
        }
    }

    suspend fun updateTranscriptionState(
        messageId: Long,
        transcript: String,
        status: TranscriptionStatus,
        error: String?
    ) {
        audioMessageDao.updateTranscriptionState(
            id = messageId,
            transcript = transcript,
            status = status.name,
            error = error
        )
    }
}