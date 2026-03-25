package com.example.tts.data.transcription


import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class VoskTranscriptionService private constructor(private val appContext: Context) {

    companion object {
        @Volatile private var instance: VoskTranscriptionService? = null

        fun get(context: Context): VoskTranscriptionService {
            val ctx = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: VoskTranscriptionService(ctx).also { instance = it }
            }
        }
    }

    @Volatile private var model: Model? = null
    private val mutex = Mutex()

    private suspend fun ensureModelLoaded(): Model = mutex.withLock {
        model?.let { return it }

        val targetDir = File(appContext.filesDir, "vosk-model-ru")
        val marker = File(targetDir, ".unpacked")

        if (!marker.exists()) {
            // Перекопируем assets/model-ru -> filesDir/vosk-model-ru
            if (targetDir.exists()) targetDir.deleteRecursively()
            targetDir.mkdirs()

            copyAssetFolder(appContext, "model-ru", targetDir)

            // Маркер, что копирование завершено
            marker.writeText("ok")
        }

        val loaded = Model(targetDir.absolutePath)
        model = loaded
        loaded
    }

    /**
     * WAV: 16kHz, mono, PCM16
     */
    suspend fun transcribeWav(wavFile: File, sampleRate: Float = 16000.0f): String =
        withContext(Dispatchers.IO) {

            if (!wavFile.exists() || wavFile.length() < 60) return@withContext "Транскрипт пока пустой"

            val m = ensureModelLoaded()
            val rec = Recognizer(m, sampleRate)

            try {
                FileInputStream(wavFile).use { fis ->
                    val skipped = fis.skip(44) // WAV header
                    if (skipped != 44L) return@withContext "Транскрипт пока пустой"

                    val buf = ByteArray(4096)
                    while (true) {
                        val n = fis.read(buf)
                        if (n <= 0) break
                        rec.acceptWaveForm(buf, n)
                    }
                }

                val json = rec.finalResult
                val text = JSONObject(json).optString("text", "").trim()
                if (text.isBlank()) "Транскрипт пока пустой" else text
            } finally {
                rec.close()
            }
        }

    private fun copyAssetFolder(context: Context, assetPath: String, outDir: File) {
        val assets = context.assets
        val items = assets.list(assetPath) ?: emptyArray()

        if (items.isEmpty()) {
            // Это файл
            val outFile = outDir
            outFile.parentFile?.mkdirs()
            assets.open(assetPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            return
        }

        // Это папка
        items.forEach { name ->
            val childAssetPath = "$assetPath/$name"
            val childItems = assets.list(childAssetPath) ?: emptyArray()

            if (childItems.isEmpty()) {
                // файл
                val outFile = File(outDir, name)
                assets.open(childAssetPath).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // папка
                val childDir = File(outDir, name)
                childDir.mkdirs()
                copyAssetFolder(context, childAssetPath, childDir)
            }
        }
    }
}