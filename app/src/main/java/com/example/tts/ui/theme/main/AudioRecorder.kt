package com.example.tts.ui.theme.main

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.tts.ui.components.AppSectionCard
import com.example.tts.ui.components.AppSectionTitle
import com.example.tts.ui.components.AppStatusBadge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicBoolean

private enum class RecorderPhase {
    IDLE,
    RECORDING,
    SAVING
}

@Composable
fun AudioRecorder(
    onSaveRecording: (filePath: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var phase by remember { mutableStateOf(RecorderPhase.IDLE) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var recordThread by remember { mutableStateOf<Thread?>(null) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var elapsedMs by remember { mutableLongStateOf(0L) }

    val stopFlag = remember { AtomicBoolean(false) }

    val isRecording = phase == RecorderPhase.RECORDING
    val isSaving = phase == RecorderPhase.SAVING

    LaunchedEffect(isRecording, startedAt) {
        while (isRecording) {
            elapsedMs = System.currentTimeMillis() - startedAt
            delay(200L)
        }
    }

    fun showError(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun startRecording() {
        if (isRecording || isSaving) return

        try {
            val recordsDir = File(context.filesDir, "audio_records").apply {
                if (!exists()) mkdirs()
            }

            val file = File(recordsDir, "rec_${System.currentTimeMillis()}.wav")
            currentFile = file
            stopFlag.set(false)
            elapsedMs = 0L
            startedAt = System.currentTimeMillis()

            val thread = Thread {
                try {
                    recordWavToFile(file, stopFlag)
                } catch (e: Exception) {
                    scope.launch {
                        phase = RecorderPhase.IDLE
                        currentFile = null
                        recordThread = null
                        elapsedMs = 0L
                        startedAt = 0L
                        showError(e.message ?: "Не удалось начать запись")
                    }
                }
            }

            recordThread = thread
            phase = RecorderPhase.RECORDING
            thread.start()
        } catch (e: Exception) {
            phase = RecorderPhase.IDLE
            currentFile = null
            recordThread = null
            elapsedMs = 0L
            startedAt = 0L
            showError(e.message ?: "Ошибка запуска записи")
        }
    }

    fun stopRecording() {
        if (!isRecording) return

        val file = currentFile
        val thread = recordThread

        stopFlag.set(true)
        phase = RecorderPhase.SAVING

        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    thread?.join()
                } catch (_: Exception) {
                }
            }

            recordThread = null
            phase = RecorderPhase.IDLE
            startedAt = 0L
            elapsedMs = 0L

            val savedFile = file?.takeIf { it.exists() }
            currentFile = null

            if (savedFile != null) {
                onSaveRecording(savedFile.absolutePath)
            } else {
                showError("Файл записи не найден после остановки")
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)

        AppSectionCard {
            AppSectionTitle(
                title = "Новая запись",
                subtitle = when (phase) {
                    RecorderPhase.IDLE -> "Запиши мысль, идею или короткую заметку."
                    RecorderPhase.RECORDING -> "Идёт запись. Нажми кнопку ещё раз, чтобы остановить."
                    RecorderPhase.SAVING -> "Сохраняем файл и подготавливаем его к распознаванию."
                }
            )

            AppStatusBadge(
                text = when (phase) {
                    RecorderPhase.IDLE -> "Готово к записи"
                    RecorderPhase.RECORDING -> "Запись идёт"
                    RecorderPhase.SAVING -> "Сохранение"
                }
            )

            Text(
                text = formatDuration(elapsedMs),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isRecording) {
                    "Во время записи не закрывай экран приложения, чтобы потом не ловить тупые обрывы файла."
                } else {
                    "Файл сохранится локально, после чего заметка появится в истории."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (isRecording) stopRecording() else startRecording()
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "  Сохраняем..."
                    )
                } else {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = null
                    )
                    Text(
                        text = if (isRecording) "  Остановить запись" else "  Начать запись"
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

@SuppressLint("MissingPermission")
private fun recordWavToFile(
    outFile: File,
    stopFlag: AtomicBoolean
) {
    val sampleRate = 16_000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val channels = 1
    val bitsPerSample = 16

    val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    if (minBuf == AudioRecord.ERROR || minBuf == AudioRecord.ERROR_BAD_VALUE) {
        throw IllegalStateException("Не удалось определить размер буфера для записи")
    }

    val bufferSize = maxOf(minBuf, 4096)

    val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize
    )

    if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
        audioRecord.release()
        throw IllegalStateException("Не удалось инициализировать AudioRecord")
    }

    val buffer = ByteArray(bufferSize)
    var totalAudioLen = 0L

    FileOutputStream(outFile).use { fos ->
        fos.write(ByteArray(44))

        try {
            audioRecord.startRecording()

            while (!stopFlag.get()) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    fos.write(buffer, 0, read)
                    totalAudioLen += read.toLong()
                }
            }
        } finally {
            try {
                audioRecord.stop()
            } catch (_: Exception) {
            }
            audioRecord.release()
        }
    }

    writeWavHeader(
        file = outFile,
        totalAudioLen = totalAudioLen,
        sampleRate = sampleRate,
        channels = channels,
        bitsPerSample = bitsPerSample
    )
}

private fun writeWavHeader(
    file: File,
    totalAudioLen: Long,
    sampleRate: Int,
    channels: Int,
    bitsPerSample: Int
) {
    val totalDataLen = totalAudioLen + 36
    val byteRate = sampleRate * channels * bitsPerSample / 8
    val header = ByteArray(44)

    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    writeIntLE(header, 4, totalDataLen.toInt())

    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()

    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    writeIntLE(header, 16, 16)
    writeShortLE(header, 20, 1)
    writeShortLE(header, 22, channels.toShort())
    writeIntLE(header, 24, sampleRate)
    writeIntLE(header, 28, byteRate)
    writeShortLE(header, 32, (channels * bitsPerSample / 8).toShort())
    writeShortLE(header, 34, bitsPerSample.toShort())

    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    writeIntLE(header, 40, totalAudioLen.toInt())

    RandomAccessFile(file, "rw").use { raf ->
        raf.seek(0)
        raf.write(header)
    }
}

private fun writeIntLE(
    buffer: ByteArray,
    offset: Int,
    value: Int
) {
    buffer[offset] = (value and 0xff).toByte()
    buffer[offset + 1] = ((value shr 8) and 0xff).toByte()
    buffer[offset + 2] = ((value shr 16) and 0xff).toByte()
    buffer[offset + 3] = ((value shr 24) and 0xff).toByte()
}

private fun writeShortLE(
    buffer: ByteArray,
    offset: Int,
    value: Short
) {
    buffer[offset] = (value.toInt() and 0xff).toByte()
    buffer[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
}