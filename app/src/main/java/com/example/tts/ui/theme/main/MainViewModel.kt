package com.example.tts.ui.theme.main

import android.app.Application
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tts.data.local.AppDatabase
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.model.TranscriptionStatus
import com.example.tts.data.repository.AudioRepository
import com.example.tts.data.transcription.VoskTranscriptionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AudioRepository(
        AppDatabase.getInstance(application).audioMessageDao()
    )

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingFilePath: String? = null
    private var observeJob: Job? = null

    fun loadMessages(userId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observeAudioMessagesForUser(userId)
                .onStart {
                    _uiState.value = MainUiState.Loading
                }
                .catch { e ->
                    _uiState.value = MainUiState.Error(
                        e.message ?: "Ошибка загрузки данных"
                    )
                }
                .collectLatest { messages ->
                    _uiState.value = MainUiState.Success(messages)
                }
        }
    }

    fun saveRecording(
        filePath: String,
        userId: String
    ) {
        viewModelScope.launch {
            try {
                val appContext = getApplication<Application>()
                val file = File(filePath)

                if (!file.exists()) {
                    _uiState.update { MainUiState.Error("Файл записи не найден") }
                    return@launch
                }

                val durationMs = withContext(Dispatchers.IO) {
                    extractDurationMs(file.absolutePath)
                }

                val draftMessage = AudioMessage(
                    userId = userId,
                    title = file.nameWithoutExtension,
                    fileName = file.name,
                    filePath = file.absolutePath,
                    transcript = "",
                    createdAt = System.currentTimeMillis(),
                    durationMs = durationMs,
                    isFavorite = false,
                    transcriptionStatus = TranscriptionStatus.PROCESSING,
                    transcriptionError = null
                )

                val messageId = withContext(Dispatchers.IO) {
                    repository.saveAudioMessage(draftMessage)
                }

                val transcriptionResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val vosk = VoskTranscriptionService.get(appContext)
                        vosk.transcribeWav(file).trim()
                    }
                }

                withContext(Dispatchers.IO) {
                    if (transcriptionResult.isSuccess) {
                        repository.updateTranscriptionState(
                            messageId = messageId,
                            transcript = transcriptionResult.getOrNull().orEmpty(),
                            status = TranscriptionStatus.COMPLETED,
                            error = null
                        )
                    } else {
                        repository.updateTranscriptionState(
                            messageId = messageId,
                            transcript = "",
                            status = TranscriptionStatus.ERROR,
                            error = transcriptionResult.exceptionOrNull()?.message
                                ?: "Неизвестная ошибка распознавания"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { MainUiState.Error(e.message ?: "Save failed") }
            }
        }
    }

    fun retryTranscription(message: AudioMessage) {
        if (message.id == 0L) return

        viewModelScope.launch {
            try {
                val appContext = getApplication<Application>()
                val file = File(message.filePath)

                if (!file.exists()) {
                    withContext(Dispatchers.IO) {
                        repository.updateTranscriptionState(
                            messageId = message.id,
                            transcript = "",
                            status = TranscriptionStatus.ERROR,
                            error = "Файл записи не найден"
                        )
                    }
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    repository.updateTranscriptionState(
                        messageId = message.id,
                        transcript = "",
                        status = TranscriptionStatus.PROCESSING,
                        error = null
                    )
                }

                val transcriptionResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val vosk = VoskTranscriptionService.get(appContext)
                        vosk.transcribeWav(file).trim()
                    }
                }

                withContext(Dispatchers.IO) {
                    if (transcriptionResult.isSuccess) {
                        repository.updateTranscriptionState(
                            messageId = message.id,
                            transcript = transcriptionResult.getOrNull().orEmpty(),
                            status = TranscriptionStatus.COMPLETED,
                            error = null
                        )
                    } else {
                        repository.updateTranscriptionState(
                            messageId = message.id,
                            transcript = "",
                            status = TranscriptionStatus.ERROR,
                            error = transcriptionResult.exceptionOrNull()?.message
                                ?: "Неизвестная ошибка распознавания"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    MainUiState.Error(e.message ?: "Не удалось повторить распознавание")
                }
            }
        }
    }

    fun playRecording(filePath: String) {
        stopCurrentPlayback()

        try {
            val file = File(filePath)
            if (!file.exists()) return

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    stopCurrentPlayback()
                }
            }

            currentPlayingFilePath = filePath
        } catch (e: Exception) {
            e.printStackTrace()
            stopCurrentPlayback()
        }
    }

    fun stopCurrentPlayback() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {
            }
            release()
        }

        mediaPlayer = null
        currentPlayingFilePath = null
    }

    fun isPlaying(filePath: String): Boolean {
        return currentPlayingFilePath == filePath && mediaPlayer?.isPlaying == true
    }

    fun deleteRecording(message: AudioMessage) {
        if (isPlaying(message.filePath)) {
            stopCurrentPlayback()
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(message.filePath)
                if (file.exists()) {
                    file.delete()
                }

                repository.deleteAudioMessage(message)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    MainUiState.Error(e.message ?: "Delete failed")
                }
            }
        }
    }

    fun toggleFavorite(message: AudioMessage) {
        if (message.id == 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setFavorite(
                    messageId = message.id,
                    isFavorite = !message.isFavorite
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    MainUiState.Error(e.message ?: "Не удалось изменить избранное")
                }
            }
        }
    }

    fun updateMessage(
        message: AudioMessage,
        newTitle: String,
        newTranscript: String
    ) {
        if (message.id == 0L) return

        val safeTitle = newTitle.trim().ifBlank {
            message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
        }
        val safeTranscript = newTranscript.trim()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateAudioMessage(
                    messageId = message.id,
                    title = safeTitle,
                    transcript = safeTranscript
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    MainUiState.Error(e.message ?: "Не удалось обновить запись")
                }
            }
        }
    }

    override fun onCleared() {
        stopCurrentPlayback()
        super.onCleared()
    }

    private fun extractDurationMs(filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        return MainViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(val messages: List<AudioMessage>) : MainUiState
    data class Error(val message: String) : MainUiState
}