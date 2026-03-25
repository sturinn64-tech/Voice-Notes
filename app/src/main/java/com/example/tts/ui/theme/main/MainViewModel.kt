package com.example.tts.ui.theme.main

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tts.R
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.repository.AudioRepository
import com.example.tts.data.transcription.VoskTranscriptionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File



class MainViewModel : ViewModel() {

    private val repository = AudioRepository()

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingFile: String? = null

    fun loadMessages(userId: String) {
        viewModelScope.launch {
            try {
                val messages = repository.getAudioMessagesForUser(userId)
                _uiState.update { MainUiState.Success(messages) }
            } catch (e: Exception) {
                _uiState.update { MainUiState.Error(e.message ?: "Unknown error") }
            }
        }
    }

    fun saveRecording(fileName: String, userId: String, context: Context) {
        viewModelScope.launch {
            try {
                val file = File(context.cacheDir, fileName)

                val vosk = VoskTranscriptionService.get(context)
                val transcript = try {
                    vosk.transcribeWav(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Ошибка распознавания: ${e.message}"
                }

                repository.saveAudioMessage(
                    AudioMessage(
                        userId = userId,
                        fileName = fileName,
                        transcript = transcript
                    )
                )

                loadMessages(userId)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { MainUiState.Error(e.message ?: "Save failed") }
            }
        }
    }

    fun playRecording(context: Context, fileName: String) {
        stopCurrentPlayback()
        try {
            val file = File(context.cacheDir, fileName)
            if (!file.exists()) return

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener { stopCurrentPlayback() }
            }
            currentPlayingFile = fileName
        } catch (e: Exception) {
            e.printStackTrace()
            stopCurrentPlayback()
        }
    }

    fun stopCurrentPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        currentPlayingFile = null
    }

    fun isPlaying(fileName: String): Boolean {
        return currentPlayingFile == fileName && mediaPlayer?.isPlaying == true
    }

    override fun onCleared() {
        stopCurrentPlayback()
        super.onCleared()
    }

    fun deleteRecording(message: com.example.tts.data.model.AudioMessage, context: Context) {
        viewModelScope.launch {
            try {
                if (isPlaying(message.fileName)) {
                    stopCurrentPlayback()
                }

                runCatching {
                    File(context.cacheDir, message.fileName).delete()
                }

                repository.deleteAudioMessage(message)

                val uid = if (message.userId.isNotBlank()) message.userId else return@launch
                loadMessages(uid)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { MainUiState.Error(e.message ?: "Delete failed") }
            }
        }
    }
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(val messages: List<AudioMessage>) : MainUiState
    data class Error(val message: String) : MainUiState
}