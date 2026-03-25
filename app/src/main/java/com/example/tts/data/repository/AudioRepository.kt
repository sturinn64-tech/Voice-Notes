package com.example.tts.data.repository

import com.example.tts.data.model.AudioMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class AudioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("audio_messages")

    suspend fun saveAudioMessage(message: AudioMessage) {
        val data = hashMapOf(
            "userId" to message.userId,
            "fileName" to message.fileName,
            "transcript" to message.transcript,
            "timestamp" to message.timestamp
        )
        collection.add(data).await()
    }

    suspend fun getAudioMessagesForUser(userId: String): List<AudioMessage> {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(AudioMessage::class.java)?.copy(id = doc.id)
        }.sortedByDescending { it.timestamp.toDate().time }
    }

    suspend fun deleteAudioMessage(message: AudioMessage) {
        if (message.id.isNotBlank()) {
            collection.document(message.id).delete().await()
            return
        }

        val snap = collection
            .whereEqualTo("userId", message.userId)
            .whereEqualTo("fileName", message.fileName)
            .get()
            .await()

        for (doc in snap.documents) {
            doc.reference.delete().await()
        }
    }
}