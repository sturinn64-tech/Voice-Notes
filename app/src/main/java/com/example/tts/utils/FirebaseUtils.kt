package com.example.tts.utils

import android.annotation.SuppressLint
import com.example.tts.data.model.AudioMessage
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {
    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    fun saveAudioMessage(message: AudioMessage, onSuccess: () -> Unit = {}) {
        db.collection("audioMessages")
            .add(message)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun getAudioMessagesForUser(userId: String, onResult: (List<AudioMessage>) -> Unit) {
        db.collection("audioMessages")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val messages = snapshot.toObjects(AudioMessage::class.java)
                onResult(messages)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onResult(emptyList())
            }
    }
}