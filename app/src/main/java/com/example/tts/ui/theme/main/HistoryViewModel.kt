package com.example.tts.ui.theme.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tts.data.local.AppDatabase
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.model.TranscriptionStatus
import com.example.tts.data.repository.AudioRepository
import com.example.tts.data.settings.HistorySortOption
import com.example.tts.data.transcription.VoskTranscriptionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val isLoading: Boolean = false,
    val messages: List<AudioMessage> = emptyList(),

    val searchQuery: String = "",
    val sortOption: HistorySortOption = HistorySortOption.NEWEST,
    val favoritesOnly: Boolean = false,

    val selectedFolder: String? = null,
    val selectedTag: String? = null,
    val folders: List<String> = emptyList(),
    val tags: List<String> = emptyList(),

    val isTrashMode: Boolean = false,

    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val currentlyPlayingPath: String? = null
)

class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AudioRepository(
        AppDatabase.getInstance(application).audioMessageDao()
    )

    private val playbackManager = AudioPlaybackManager()
    private val voskService = VoskTranscriptionService.get(application)
    private val exportManager = ExportManager(application)

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var observeActiveJob: Job? = null
    private var observeTrashJob: Job? = null
    private var currentUserId: String? = null

    private var activeMessages: List<AudioMessage> = emptyList()
    private var trashMessages: List<AudioMessage> = emptyList()

    fun loadMessages(userId: String) {
        if (currentUserId == userId && observeActiveJob != null && observeTrashJob != null) return

        currentUserId = userId
        observeActiveJob?.cancel()
        observeTrashJob?.cancel()

        _uiState.update { it.copy(isLoading = true) }

        observeActiveJob = viewModelScope.launch {
            repository.observeAudioMessagesForUser(userId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Ошибка загрузки истории"
                        )
                    }
                }
                .collect { messages ->
                    activeMessages = messages
                    applyFilters()
                }
        }

        observeTrashJob = viewModelScope.launch {
            repository.observeDeletedAudioMessagesForUser(userId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Ошибка загрузки корзины"
                        )
                    }
                }
                .collect { messages ->
                    trashMessages = messages
                    applyFilters()
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateSortOption(option: HistorySortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFilters()
    }

    fun toggleFavoritesOnly() {
        _uiState.update { it.copy(favoritesOnly = !it.favoritesOnly) }
        applyFilters()
    }

    fun selectFolder(folderName: String?) {
        _uiState.update {
            it.copy(
                selectedFolder = folderName?.takeIf { folder -> folder.isNotBlank() }
            )
        }
        applyFilters()
    }

    fun selectTag(tag: String?) {
        _uiState.update {
            it.copy(
                selectedTag = tag?.takeIf { value -> value.isNotBlank() }
            )
        }
        applyFilters()
    }

    fun toggleTrashMode() {
        _uiState.update {
            it.copy(
                isTrashMode = !it.isTrashMode,
                selectedFolder = null,
                selectedTag = null,
                favoritesOnly = false
            )
        }
        applyFilters()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun playOrStop(filePath: String) {
        val current = _uiState.value.currentlyPlayingPath

        if (current == filePath && playbackManager.isPlaying(filePath)) {
            playbackManager.stop()
            _uiState.update { it.copy(currentlyPlayingPath = null) }
            return
        }

        playbackManager.play(
            filePath = filePath,
            onStarted = {
                _uiState.update { state ->
                    state.copy(currentlyPlayingPath = filePath)
                }
            },
            onCompleted = {
                _uiState.update { state ->
                    state.copy(currentlyPlayingPath = null)
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        currentlyPlayingPath = null,
                        errorMessage = error
                    )
                }
            }
        )
    }

    fun deleteRecording(message: AudioMessage) {
        moveToTrash(message)
    }

    fun moveToTrash(message: AudioMessage) {
        if (message.id == 0L) return

        if (_uiState.value.currentlyPlayingPath == message.filePath) {
            playbackManager.stop()
            _uiState.update { it.copy(currentlyPlayingPath = null) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.moveToTrash(message.id)
                _uiState.update {
                    it.copy(infoMessage = "Запись перемещена в корзину")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось переместить запись в корзину")
                }
            }
        }
    }

    fun restoreRecording(message: AudioMessage) {
        if (message.id == 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.restoreFromTrash(message.id)
                _uiState.update {
                    it.copy(infoMessage = "Запись восстановлена")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось восстановить запись")
                }
            }
        }
    }

    fun deleteForever(message: AudioMessage) {
        if (message.id == 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(message.filePath)

                if (file.exists() && !file.delete()) {
                    _uiState.update {
                        it.copy(errorMessage = "Не удалось удалить аудиофайл с устройства")
                    }
                    return@launch
                }

                repository.deleteAudioMessage(message)

                _uiState.update {
                    it.copy(infoMessage = "Запись удалена навсегда")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось удалить запись")
                }
            }
        }
    }

    fun emptyTrash() {
        val userId = currentUserId ?: return
        val messagesToDelete = trashMessages

        viewModelScope.launch(Dispatchers.IO) {
            try {
                messagesToDelete.forEach { message ->
                    val file = File(message.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                repository.emptyTrash(userId)

                _uiState.update {
                    it.copy(infoMessage = "Корзина очищена")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось очистить корзину")
                }
            }
        }
    }

    fun toggleFavorite(message: AudioMessage) {
        if (message.id == 0L || message.isDeleted) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setFavorite(
                    messageId = message.id,
                    isFavorite = !message.isFavorite
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось изменить избранное")
                }
            }
        }
    }

    fun updateMessage(
        message: AudioMessage,
        newTitle: String,
        newTranscript: String
    ) {
        if (message.id == 0L || message.isDeleted) return

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
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось обновить запись")
                }
            }
        }
    }

    fun updateFolder(
        message: AudioMessage,
        folderName: String
    ) {
        if (message.id == 0L || message.isDeleted) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateFolder(
                    messageId = message.id,
                    folderName = folderName.trim()
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось изменить папку")
                }
            }
        }
    }

    fun addTag(
        message: AudioMessage,
        tag: String
    ) {
        if (message.id == 0L || message.isDeleted) return

        val safeTag = tag.trim().removePrefix("#")
        if (safeTag.isBlank()) return

        val updatedTags = (message.tags.map { it.name } + safeTag)
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.getDefault()) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateTags(
                    messageId = message.id,
                    tags = updatedTags
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось добавить тег")
                }
            }
        }
    }

    fun removeTag(
        message: AudioMessage,
        tag: String
    ) {
        if (message.id == 0L || message.isDeleted) return

        val tagToRemove = tag.trim().removePrefix("#")

        val updatedTags = message.tags
            .map { it.name }
            .filterNot {
                it.equals(tagToRemove, ignoreCase = true)
            }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateTags(
                    messageId = message.id,
                    tags = updatedTags
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось удалить тег")
                }
            }
        }
    }

    fun retryTranscription(message: AudioMessage) {
        if (message.id == 0L || message.isDeleted) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(message.filePath)

                if (!file.exists()) {
                    _uiState.update {
                        it.copy(errorMessage = "Файл записи не найден")
                    }
                    return@launch
                }

                repository.updateTranscriptionState(
                    messageId = message.id,
                    transcript = "",
                    status = TranscriptionStatus.PROCESSING,
                    error = null
                )

                val result = runCatching {
                    voskService.transcribeWav(file).trim()
                }

                if (result.isSuccess) {
                    repository.updateTranscriptionState(
                        messageId = message.id,
                        transcript = result.getOrNull().orEmpty(),
                        status = TranscriptionStatus.COMPLETED,
                        error = null
                    )
                } else {
                    repository.updateTranscriptionState(
                        messageId = message.id,
                        transcript = "",
                        status = TranscriptionStatus.ERROR,
                        error = result.exceptionOrNull()?.message ?: "Ошибка распознавания"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось повторить распознавание")
                }
            }
        }
    }

    fun exportText(message: AudioMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val baseName = buildBaseName(message)
                val displayName = "$baseName.txt"
                val content = buildTextExportContent(message)

                exportManager.exportTextFile(
                    displayName = displayName,
                    content = content
                )

                _uiState.update {
                    it.copy(infoMessage = "Текст экспортирован в Downloads/VoiceNotes")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось экспортировать текст")
                }
            }
        }
    }

    fun exportAudio(message: AudioMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sourceFile = File(message.filePath)

                if (!sourceFile.exists()) {
                    _uiState.update {
                        it.copy(errorMessage = "Аудиофайл не найден")
                    }
                    return@launch
                }

                val extension = sourceFile.extension.ifBlank { "wav" }
                val baseName = buildBaseName(message)
                val displayName = "$baseName.$extension"

                exportManager.exportAudioFile(
                    sourcePath = message.filePath,
                    displayName = displayName,
                    mimeType = resolveAudioMimeType(extension)
                )

                _uiState.update {
                    it.copy(infoMessage = "Аудио экспортировано в Downloads/VoiceNotes")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Не удалось экспортировать аудио")
                }
            }
        }
    }

    private fun buildBaseName(message: AudioMessage): String {
        val raw = message.title.ifBlank {
            message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
        }

        return raw
            .trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .replace("\n", " ")
            .ifBlank { "voice_note_${message.createdAt}" }
    }

    private fun buildTextExportContent(message: AudioMessage): String {
        val title = message.title.ifBlank {
            message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
        }

        val dateText = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(message.createdAt))

        val transcriptText = message.transcript.ifBlank { "Текст отсутствует" }
        val folderText = message.folder?.name?.takeIf { it.isNotBlank() } ?: "Без папки"
        val tagsText = message.tags
            .map { it.name }
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { "Нет тегов" }

        return buildString {
            appendLine("Название: $title")
            appendLine("Дата: $dateText")
            appendLine("Файл: ${message.fileName}")
            appendLine("Папка: $folderText")
            appendLine("Теги: $tagsText")
            appendLine()
            appendLine("Текст заметки:")
            appendLine(transcriptText)
        }
    }

    private fun resolveAudioMimeType(extension: String): String {
        return when (extension.lowercase(Locale.getDefault())) {
            "wav" -> "audio/wav"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            else -> "audio/*"
        }
    }

    private fun applyFilters() {
        val state = _uiState.value

        val sourceMessages = if (state.isTrashMode) {
            trashMessages
        } else {
            activeMessages
        }

        val folders = activeMessages
            .mapNotNull { it.folder?.name?.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })

        val tags = activeMessages
            .flatMap { it.tags }
            .map { it.name.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })

        val query = state.searchQuery.trim().lowercase(Locale.getDefault())

        val filtered = sourceMessages
            .asSequence()
            .filter { !state.favoritesOnly || it.isFavorite }
            .filter { message ->
                val selectedFolder = state.selectedFolder

                selectedFolder.isNullOrBlank() ||
                        message.folder?.name.equals(
                            selectedFolder,
                            ignoreCase = true
                        )
            }
            .filter { message ->
                val selectedTag = state.selectedTag

                selectedTag.isNullOrBlank() ||
                        message.tags.any { tag ->
                            tag.name.equals(
                                selectedTag,
                                ignoreCase = true
                            )
                        }
            }
            .filter { message ->
                if (query.isBlank()) return@filter true

                val title = message.title.lowercase(Locale.getDefault())
                val fileName = message.fileName.lowercase(Locale.getDefault())
                val transcript = message.transcript.lowercase(Locale.getDefault())
                val folderName = message.folder?.name.orEmpty().lowercase(Locale.getDefault())
                val tagText = message.tags
                    .joinToString(" ") { it.name }
                    .lowercase(Locale.getDefault())

                query in title ||
                        query in fileName ||
                        query in transcript ||
                        query in folderName ||
                        query in tagText
            }
            .toList()

        val sorted = when (state.sortOption) {
            HistorySortOption.NEWEST -> filtered.sortedWith(
                compareByDescending<AudioMessage> { it.isFavorite }
                    .thenByDescending { it.createdAt }
            )

            HistorySortOption.OLDEST -> filtered.sortedWith(
                compareByDescending<AudioMessage> { it.isFavorite }
                    .thenBy { it.createdAt }
            )

            HistorySortOption.TITLE -> filtered.sortedWith(
                compareByDescending<AudioMessage> { it.isFavorite }
                    .thenBy {
                        it.title.ifBlank { it.fileName }.lowercase(Locale.getDefault())
                    }
            )
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                messages = sorted,
                folders = folders,
                tags = tags
            )
        }
    }

    override fun onCleared() {
        playbackManager.release()
        super.onCleared()
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                        return HistoryViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}