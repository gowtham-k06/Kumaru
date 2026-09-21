package com.kumaru.assistant.domain.voice

/**
 * Abstraction for speech recognition and voice capture.
 *
 * For V0.1, push-to-talk is utilized.
 * In future stages, continuous listening and the wake-phrase ("Enna Kumaru")
 * will hook into this abstraction.
 */
interface VoiceInput {
    /**
     * Starts audio capture / speech recognition session.
     *
     * @param onResult Callback invoked when final speech transcription is complete.
     * @param onError Callback invoked when an error occurs during capture.
     * @param onPartialResult Optional callback invoked as partial speech is transcribed in real-time.
     * @param onNoSpeech Optional callback invoked when the session finishes but no speech was captured.
     * @param onActivityDetected Optional callback invoked whenever speech/audio activity is detected.
     */
    fun startListening(
        onResult: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onPartialResult: ((String) -> Unit)? = null,
        onNoSpeech: (() -> Unit)? = null,
        onActivityDetected: (() -> Unit)? = null
    )

    /**
     * Stops the ongoing listening session.
     */
    fun stopListening()

    /**
     * Current listening state.
     */
    val isListening: Boolean

    /**
     * Release any underlying platform resources (e.g. SpeechRecognizer).
     */
    fun destroy() {}
}

