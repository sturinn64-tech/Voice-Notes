package com.example.tts.ui.theme.main

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun AudioRecorder(
    onSaveRecording: (filePath: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var recordThread by remember { mutableStateOf<Thread?>(null) }
    var startedAt by remember { mutableStateOf(0L) }
    var elapsedText by remember { mutableStateOf("00:00") }

    val stopFlag = remember { AtomicBoolean(false) }

    LaunchedEffect(isRecording, startedAt) {
        if (!isRecording) {
            elapsedText = "00:00"
            return@LaunchedEffect
        }

        while (isRecording) {
            val elapsed = System.currentTimeMillis() - startedAt
            elapsedText = formatElapsed(elapsed)
            delay(500)
        }
    }

    fun startRecording() {
        if (isRecording) return

        stopFlag.set(false)

        val recordsDir = File(context.filesDir, "audio_records").apply {
            if (!exists()) mkdirs()
        }

        val file = File(recordsDir, "rec_${System.currentTimeMillis()}.wav")
        currentFile = file
        startedAt = System.currentTimeMillis()

        val thread = Thread {
            recordWavToFile(file, stopFlag)
        }

        recordThread = thread
        thread.start()
        isRecording = true
    }

    fun stopRecording() {
        if (!isRecording) return

        stopFlag.set(true)
        val file = currentFile
        val thread = recordThread

        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    thread?.join()
                } catch (_: Exception) {
                }
            }

            isRecording = false
            recordThread = null
            startedAt = 0L

            file?.let { onSaveRecording(it.absolutePath) }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isRecording) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = if (isRecording) {
                        "Идет запись • $elapsedText"
                    } else {
                        "Готово к записи"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isRecording) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Новая голосовая заметка",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = if (isRecording) {
                        "Нажми кнопку еще раз, чтобы остановить запись и сохранить файл."
                    } else {
                        "Запись сохранится локально и сразу появится в истории."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    if (isRecording) stopRecording() else startRecording()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                colors = if (isRecording) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Icon(
                    imageVector = if (isRecording) {
                        Icons.Filled.Stop
                    } else {
                        Icons.Filled.Mic
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isRecording) "Остановить запись" else "Начать запись",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Подсказка",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Для лучшего качества говори ближе к микрофону и в тихом помещении.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatElapsed(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@SuppressLint("MissingPermission")
private fun recordWavToFile(
    outFile: File,
    stopFlag: AtomicBoolean
) {
    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val channels = 1
    val bitsPerSample = 16

    val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    val bufferSize = maxOf(minBuf, 4096)

    val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize
    )

    if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
        throw IllegalStateException("Не удалось инициализировать AudioRecord")
    }

    val buffer = ByteArray(bufferSize)
    var totalAudioLen = 0L

    FileOutputStream(outFile).use { fos ->
        fos.write(ByteArray(44))

        audioRecord.startRecording()

        try {
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

    writeWavHeader(outFile, totalAudioLen, sampleRate, channels, bitsPerSample)
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