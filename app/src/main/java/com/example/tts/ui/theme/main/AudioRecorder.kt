package com.example.tts.ui.theme.main


import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun AudioRecorder(
    onSaveRecording: (fileName: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var recordThread by remember { mutableStateOf<Thread?>(null) }
    val stopFlag = remember { AtomicBoolean(false) }

    fun startRecording() {
        if (isRecording) return
        stopFlag.set(false)

        val file = File(context.cacheDir, "rec_${System.currentTimeMillis()}.wav")
        currentFile = file

        val t = Thread { recordWavToFile(file, stopFlag) }
        recordThread = t
        t.start()
        isRecording = true
    }

    fun stopRecording() {
        if (!isRecording) return
        stopFlag.set(true)

        val file = currentFile
        val t = recordThread

        scope.launch {
            withContext(Dispatchers.IO) { try { t?.join() } catch (_: Exception) {} }
            isRecording = false
            recordThread = null
            file?.let { onSaveRecording(it.name) }
        }
    }

    Button(onClick = { if (isRecording) stopRecording() else startRecording() }) {
        Text(if (isRecording) "Остановить запись" else "Начать запись")
    }
}

@SuppressLint("MissingPermission")
private fun recordWavToFile(outFile: File, stopFlag: AtomicBoolean) {
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

    val buffer = ByteArray(bufferSize)
    var totalAudioLen = 0L

    FileOutputStream(outFile).use { fos ->
        fos.write(ByteArray(44)) // WAV header placeholder
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
            try { audioRecord.stop() } catch (_: Exception) {}
            audioRecord.release()
        }
    }

    writeWavHeader(outFile, totalAudioLen, sampleRate, channels, bitsPerSample)
}

private fun writeWavHeader(file: File, totalAudioLen: Long, sampleRate: Int, channels: Int, bitsPerSample: Int) {
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

private fun writeIntLE(b: ByteArray, offset: Int, value: Int) {
    b[offset] = (value and 0xff).toByte()
    b[offset + 1] = ((value shr 8) and 0xff).toByte()
    b[offset + 2] = ((value shr 16) and 0xff).toByte()
    b[offset + 3] = ((value shr 24) and 0xff).toByte()
}

private fun writeShortLE(b: ByteArray, offset: Int, value: Short) {
    b[offset] = (value.toInt() and 0xff).toByte()
    b[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
}