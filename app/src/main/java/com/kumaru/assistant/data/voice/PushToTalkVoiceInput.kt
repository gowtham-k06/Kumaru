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
 * Production Android SpeechRecognizer implementation of [VoiceInput] for Kumaru V0.2.
 * Captures real microphone audio, streams partial transcriptions, and returns
 * the final recognized text to the assistant pipeline.
 */
class PushToTalkVoiceInput(
    private val contextProvider: () -> Context? = { runCatching { KumaruApplication.appContext }.getOrNull() }
) : VoiceInput {

    companion object {
        private const val TAG = "PushToTalkVoiceInput"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private var _isListening: Boolean = false
    override val isListening: Boolean
        get() = _isListening

    private var resultCallback: ((String) -> Unit)? = null
    private var errorCallback: ((Throwable) -> Unit)? = null
    private var partialResultCallback: ((String) -> Unit)? = null

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onPartialResult: ((String) -> Unit)?
    ) {
        resultCallback = onResult
        errorCallback = onError
        partialResultCallback = onPartialResult

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
                // Safely destroy previous instance if still active
                cleanupRecognizer()

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(createRecognitionListener())

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

                _isListening = true
                recognizer.startListening(intent)
                Log.d(TAG, "Speech recognition started successfully")
            } catch (e: Exception) {
                _isListening = false
                Log.e(TAG, "Failed to start speech recognition: ${e.message}", e)
                onError(e)
            }
        }
    }

    override fun stopListening() {
        mainHandler.post {
            if (!_isListening) return@post
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping SpeechRecognizer: ${e.message}")
            }
        }
    }

    override fun destroy() {
        mainHandler.post {
            cleanupRecognizer()
            _isListening = false
            resultCallback = null
            errorCallback = null
            partialResultCallback = null
        }
    }

    /**
     * Allows dispatching an explicit user input string while honoring the VoiceInput contract.
     */
    fun submitTranscript(text: String) {
        _isListening = false
        val callback = resultCallback
        clearCallbacks()
        callback?.invoke(text)
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
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "SpeechRecognizer: Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "SpeechRecognizer: Beginning of speech detected")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level updates (can be used for visual waveform in future stages)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "SpeechRecognizer: End of speech detected")
            }

            override fun onError(error: Int) {
                _isListening = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check your microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "I couldn't hear you."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Can't reach Gemini. Check your internet connection."
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I couldn't hear you."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please try again."
                    SpeechRecognizer.ERROR_SERVER -> "Something went wrong."
                    else -> "I couldn't hear you."
                }
                Log.e(TAG, "SpeechRecognizer error: code=$error (userMessage='$errorMessage')")

                val callback = errorCallback
                clearCallbacks()
                cleanupRecognizer()
                callback?.invoke(Exception(errorMessage))
            }

            override fun onResults(results: Bundle?) {
                _isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()

                Log.d(TAG, "SpeechRecognizer onResults: $recognizedText")

                val onRes = resultCallback
                val onErr = errorCallback
                clearCallbacks()
                cleanupRecognizer()

                if (!recognizedText.isNullOrEmpty()) {
                    onRes?.invoke(recognizedText)
                } else {
                    onErr?.invoke(Exception("No speech recognized. Please try again."))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = partialMatches?.firstOrNull()?.trim()
                if (!partialText.isNullOrEmpty()) {
                    partialResultCallback?.invoke(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}

