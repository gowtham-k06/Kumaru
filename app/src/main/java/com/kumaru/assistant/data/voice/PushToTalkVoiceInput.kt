package com.kumaru.assistant.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.kumaru.assistant.KumaruApplication
import com.kumaru.assistant.domain.voice.VoiceInput
import java.util.Locale

/**
 * Production Android SpeechRecognizer implementation of [VoiceInput] for Kumaru V0.2.1.
 * Supports manual start/stop continuous voice sessions with seamless pause handling,
 * multi-segment accumulation, and activity tracking.
 */
class PushToTalkVoiceInput(
    private val contextProvider: () -> Context? = { runCatching { KumaruApplication.appContext }.getOrNull() }
) : VoiceInput {

    companion object {
        private const val TAG = "PushToTalkVoiceInput"
        private const val RMS_ACTIVITY_THRESHOLD = 3.0f
        private const val RMS_NOTIFICATION_THROTTLE_MS = 800L
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private var _isListening: Boolean = false
    override val isListening: Boolean
        get() = _isListening

    private var resultCallback: ((String) -> Unit)? = null
    private var errorCallback: ((Throwable) -> Unit)? = null
    private var partialResultCallback: ((String) -> Unit)? = null
    private var noSpeechCallback: (() -> Unit)? = null
    private var activityDetectedCallback: (() -> Unit)? = null

    // Multi-segment transcript accumulation buffers
    private val accumulatedTranscript = StringBuilder()
    private var currentSegmentTranscript: String = ""
    private var lastRmsActivityTime: Long = 0L

    private var cachedIntent: Intent? = null

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onPartialResult: ((String) -> Unit)?,
        onNoSpeech: (() -> Unit)?,
        onActivityDetected: (() -> Unit)?
    ) {
        resultCallback = onResult
        errorCallback = onError
        partialResultCallback = onPartialResult
        noSpeechCallback = onNoSpeech
        activityDetectedCallback = onActivityDetected

        mainHandler.post {
            val context = contextProvider()
            if (context == null) {
                _isListening = false
                onError(IllegalStateException("Application context is unavailable for speech recognition."))
                return@post
            }

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _isListening = false
                Log.e(TAG, "SpeechRecognizer is not available on this device")
                onError(IllegalStateException("Speech recognition service is not available on this device."))
                return@post
            }

            try {
                // Reset accumulated text for new voice session
                accumulatedTranscript.setLength(0)
                currentSegmentTranscript = ""
                lastRmsActivityTime = 0L
                _isListening = true

                // Notify initial activity
                activityDetectedCallback?.invoke()

                val recognizer = getOrCreateRecognizer(context)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }
                cachedIntent = intent

                recognizer.startListening(intent)
                Log.d(TAG, "Speech recognition session started")
            } catch (e: Exception) {
                _isListening = false
                Log.e(TAG, "Failed to start speech recognition session: ${e.message}", e)
                onError(e)
            }
        }
    }

    override fun stopListening() {
        mainHandler.post {
            if (!_isListening) return@post
            _isListening = false

            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping SpeechRecognizer: ${e.message}")
            }

            val finalTranscript = getFullTranscript()
            val onRes = resultCallback
            val onNoSp = noSpeechCallback

            clearCallbacks()

            if (finalTranscript.isNotEmpty()) {
                Log.d(TAG, "Voice session concluded with transcript: \"$finalTranscript\"")
                onRes?.invoke(finalTranscript)
            } else {
                Log.d(TAG, "Voice session concluded with no speech detected")
                onNoSp?.invoke()
            }
        }
    }

    override fun destroy() {
        mainHandler.post {
            _isListening = false
            cleanupRecognizer()
            clearCallbacks()
            accumulatedTranscript.setLength(0)
            currentSegmentTranscript = ""
        }
    }

    private fun getOrCreateRecognizer(context: Context): SpeechRecognizer {
        speechRecognizer?.let { return it }

        val newRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        newRecognizer.setRecognitionListener(createRecognitionListener(context))
        speechRecognizer = newRecognizer
        return newRecognizer
    }

    private fun cleanupRecognizer() {
        speechRecognizer?.let { recognizer ->
            try {
                recognizer.stopListening()
                recognizer.cancel()
                recognizer.destroy()
            } catch (e: Exception) {
                Log.w(TAG, "Error destroying speech recognizer: ${e.message}")
            }
        }
        speechRecognizer = null
    }

    private fun clearCallbacks() {
        resultCallback = null
        errorCallback = null
        partialResultCallback = null
        noSpeechCallback = null
        activityDetectedCallback = null
    }

    private fun getFullTranscript(): String {
        val accumulated = accumulatedTranscript.toString().trim()
        val current = currentSegmentTranscript.trim()

        return when {
            accumulated.isNotEmpty() && current.isNotEmpty() -> "$accumulated $current"
            accumulated.isNotEmpty() -> accumulated
            else -> current
        }.trim()
    }

    private fun createRecognitionListener(context: Context): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "SpeechRecognizer: Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "SpeechRecognizer: Beginning of speech detected")
                activityDetectedCallback?.invoke()
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (rmsdB > RMS_ACTIVITY_THRESHOLD) {
                    val now = System.currentTimeMillis()
                    if (now - lastRmsActivityTime > RMS_NOTIFICATION_THROTTLE_MS) {
                        lastRmsActivityTime = now
                        activityDetectedCallback?.invoke()
                    }
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "SpeechRecognizer: End of segment speech detected (session remains active)")
            }

            override fun onError(error: Int) {
                Log.d(TAG, "SpeechRecognizer onError: code=$error")

                if (!_isListening) return

                // Non-fatal errors that occur when the user pauses or internal silence timeout triggers
                if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                    error == SpeechRecognizer.ERROR_CLIENT
                ) {
                    Log.d(TAG, "Recognized pause/silence in segment (code=$error). Session is active, restarting recognition segment...")
                    restartRecognitionSegment(context)
                    return
                }

                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Log.w(TAG, "Recognizer busy, resetting and continuing...")
                    try {
                        speechRecognizer?.cancel()
                    } catch (_: Exception) {}
                    mainHandler.postDelayed({
                        if (_isListening) {
                            restartRecognitionSegment(context)
                        }
                    }, 150L)
                    return
                }

                // Fatal error - conclude session
                _isListening = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check your microphone."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error occurred during speech recognition."
                    SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error. Please try again."
                    else -> "Speech recognition error occurred."
                }
                Log.e(TAG, "SpeechRecognizer fatal error: code=$error ($errorMessage)")

                val onErr = errorCallback
                clearCallbacks()
                cleanupRecognizer()
                onErr?.invoke(Exception(errorMessage))
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedSegment = matches?.firstOrNull()?.trim()

                Log.d(TAG, "SpeechRecognizer segment onResults: \"$recognizedSegment\"")

                if (!recognizedSegment.isNullOrEmpty()) {
                    if (accumulatedTranscript.isNotEmpty()) {
                        accumulatedTranscript.append(" ")
                    }
                    accumulatedTranscript.append(recognizedSegment)
                    currentSegmentTranscript = ""

                    val fullText = getFullTranscript()
                    partialResultCallback?.invoke(fullText)
                    activityDetectedCallback?.invoke()
                }

                // If session is still active (user hasn't tapped Stop / 15s inactivity hasn't expired),
                // continue listening for next speech segment!
                if (_isListening) {
                    restartRecognitionSegment(context)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = partialMatches?.firstOrNull()?.trim()

                if (!partialText.isNullOrEmpty()) {
                    currentSegmentTranscript = partialText
                    val fullText = getFullTranscript()
                    partialResultCallback?.invoke(fullText)
                    activityDetectedCallback?.invoke()
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun restartRecognitionSegment(context: Context) {
        mainHandler.post {
            if (!_isListening) return@post
            try {
                val recognizer = getOrCreateRecognizer(context)
                val intent = cachedIntent ?: Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }
                recognizer.startListening(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restart recognition segment: ${e.message}")
            }
        }
    }
}
