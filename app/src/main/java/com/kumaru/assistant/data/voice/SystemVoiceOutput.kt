package com.kumaru.assistant.data.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.kumaru.assistant.KumaruApplication
import com.kumaru.assistant.domain.voice.VoiceOutput
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID

/**
 * Native Android [TextToSpeech] implementation of [VoiceOutput] for Kumaru V0.2.
 * Synthesizes Gemini responses into clear vocal speech with reliable completion tracking,
 * markdown cleaning, and lifecycle management.
 */
class SystemVoiceOutput(
    private val contextProvider: () -> Context? = { runCatching { KumaruApplication.appContext }.getOrNull() }
) : VoiceOutput {

    companion object {
        private const val TAG = "SystemVoiceOutput"
        private const val INIT_TIMEOUT_MS = 5000L
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isTtsInitialized: Boolean = false
    private var initDeferred = CompletableDeferred<Boolean>()

    private var _isSpeaking: Boolean = false
    override val isSpeaking: Boolean
        get() = _isSpeaking

    private var activeUtteranceDeferred: CompletableDeferred<Unit>? = null

    init {
        initializeTts()
    }

    private fun initializeTts() {
        mainHandler.post {
            val context = contextProvider()
            if (context == null) {
                Log.w(TAG, "Application context unavailable for TextToSpeech initialization")
                initDeferred.complete(false)
                return@post
            }

            try {
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.let { engine ->
                            val result = engine.setLanguage(Locale.US)
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.w(TAG, "US English voice missing/unsupported, falling back to default locale")
                                engine.setLanguage(Locale.getDefault())
                            }
                            engine.setSpeechRate(1.0f)
                            engine.setPitch(1.0f)
                        }
                        isTtsInitialized = true
                        Log.d(TAG, "TextToSpeech engine initialized successfully")
                        initDeferred.complete(true)
                    } else {
                        Log.e(TAG, "TextToSpeech initialization failed with status: $status")
                        isTtsInitialized = false
                        initDeferred.complete(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception initializing TextToSpeech: ${e.message}", e)
                isTtsInitialized = false
                initDeferred.complete(false)
            }
        }
    }

    override suspend fun speak(text: String): Unit = withContext(Dispatchers.Main) {
        val cleanSpeechText = cleanTextForSpeech(text)
        if (cleanSpeechText.isBlank()) {
            _isSpeaking = false
            return@withContext
        }

        // Wait for TTS engine to complete initialization if in progress
        if (!isTtsInitialized) {
            val initialized = withTimeoutOrNull(INIT_TIMEOUT_MS) {
                initDeferred.await()
            } ?: false

            if (!initialized || tts == null) {
                Log.w(TAG, "TTS not ready or failed to initialize, skipping vocal synthesis")
                _isSpeaking = false
                return@withContext
            }
        }

        stop()
        _isSpeaking = true

        val utteranceId = "kumaru_speech_${UUID.randomUUID()}"
        val completionDeferred = CompletableDeferred<Unit>()
        activeUtteranceDeferred = completionDeferred

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking = false
                completionDeferred.complete(Unit)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.w(TAG, "TTS speech playback error for utterance: $utteranceId")
                _isSpeaking = false
                completionDeferred.complete(Unit)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.w(TAG, "TTS speech playback error ($errorCode) for utterance: $utteranceId")
                _isSpeaking = false
                completionDeferred.complete(Unit)
            }
        })

        try {
            val queueResult = tts?.speak(
                cleanSpeechText,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )

            if (queueResult == TextToSpeech.ERROR) {
                Log.e(TAG, "TextToSpeech speak() returned ERROR")
                _isSpeaking = false
                return@withContext
            }

            // Suspend until speech finishes or is cancelled
            completionDeferred.await()
        } catch (e: Exception) {
            Log.e(TAG, "Exception during speech synthesis: ${e.message}")
        } finally {
            _isSpeaking = false
            if (activeUtteranceDeferred == completionDeferred) {
                activeUtteranceDeferred = null
            }
        }
    }

    override fun stop() {
        mainHandler.post {
            try {
                tts?.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping TTS: ${e.message}")
            }
            _isSpeaking = false
            activeUtteranceDeferred?.complete(Unit)
            activeUtteranceDeferred = null
        }
    }

    override fun release() {
        mainHandler.post {
            stop()
            try {
                tts?.shutdown()
            } catch (e: Exception) {
                Log.w(TAG, "Error shutting down TTS: ${e.message}")
            }
            tts = null
            isTtsInitialized = false
        }
    }

    /**
     * Cleans up markdown artifacts and formatting symbols so spoken output is natural.
     */
    private fun cleanTextForSpeech(raw: String): String {
        return raw
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Bold **text**
            .replace(Regex("\\*(.*?)\\*"), "$1")       // Italic *text*
            .replace(Regex("`{1,3}.*?`{1,3}"), "")    // Code blocks/inline code
            .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "") // Headers #
            .replace(Regex("^[-*•]\\s+", RegexOption.MULTILINE), "") // Bullets
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1") // Links [text](url)
            .trim()
    }
}

