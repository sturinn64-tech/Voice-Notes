package com.example.tts.ui.theme.main

import android.media.MediaPlayer
import java.io.File

class AudioPlaybackManager {

    private var mediaPlayer: MediaPlayer? = null
    private var currentFilePath: String? = null

    fun play(
        filePath: String,
        onStarted: () -> Unit,
        onCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        stop()

        val file = File(filePath)
        if (!file.exists()) {
            onError("Файл для воспроизведения не найден")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)

                setOnPreparedListener {
                    start()
                    currentFilePath = filePath
                    onStarted()
                }

                setOnCompletionListener {
                    stop()
                    onCompleted
                }

                setOnErrorListener { _, _, _ ->
                    stop()
                    onError("Ошибка воспроизведения")
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            stop()
            onError(e.message ?: "Не удалось воспроизвести запись")
        }
    }

    fun isPlaying(filePath: String): Boolean {
        return currentFilePath == filePath && mediaPlayer?.isPlaying == true
    }

    fun stop() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {

            }

            try {
                reset()
            } catch (_: Exception) {

            }

            try {
                release()
            } catch (_: Exception) {

            }
        }

        mediaPlayer = null
        currentFilePath = null
    }

    fun release() {
        stop()
    }
}