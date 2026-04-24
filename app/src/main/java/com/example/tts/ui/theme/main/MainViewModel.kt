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

data class MainUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AudioRepository(
        AppDatabase.getInstance(application).audioMessageDao()
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun saveRecording(
        filePath: String,
        userId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            try {
                val appContext = getApplication<Application>()
                val file = File(filePath)

                if (!file.exists()) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Файл записи не найден"
                        )
                    }
                    return@launch
                }

                val durationMs = extractDurationMs(file.absolutePath)

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

                val messageId = repository.saveAudioMessage(draftMessage)

                val transcriptionResult = runCatching {
                    val vosk = VoskTranscriptionService.get(appContext)
                    vosk.transcribeWav(file).trim()
                }

                if (transcriptionResult.isSuccess) {
                    repository.updateTranscriptionState(
                        messageId = messageId,
                        transcript = transcriptionResult.getOrNull().orEmpty(),
                        status = TranscriptionStatus.COMPLETED,
                        error = null
                    )

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            infoMessage = "Запись сохранена"
                        )
                    }
                } else {
                    repository.updateTranscriptionState(
                        messageId = messageId,
                        transcript = "",
                        status = TranscriptionStatus.ERROR,
                        error = transcriptionResult.exceptionOrNull()?.message
                            ?: "Неизвестная ошибка распознавания"
                    )

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = transcriptionResult.exceptionOrNull()?.message
                                ?: "Не удалось распознать запись"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Не удалось сохранить запись"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfo() {
        _uiState.update { it.copy(infoMessage = null) }
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