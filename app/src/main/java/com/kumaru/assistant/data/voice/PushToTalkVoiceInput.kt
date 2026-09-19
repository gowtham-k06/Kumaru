package com.kumaru.assistant.data.voice

import com.kumaru.assistant.domain.voice.VoiceInput

/**
 * Push-to-talk implementation of [VoiceInput] for V0.1.
 * Provides the interactive speech input simulation and state coordinator.
 */
class PushToTalkVoiceInput : VoiceInput {

    private var _isListening: Boolean = false
    override val isListening: Boolean
        get() = _isListening

    private var resultCallback: ((String) -> Unit)? = null
    private var errorCallback: ((Throwable) -> Unit)? = null

    // Sample conversational prompts for V0.1 push-to-talk demonstration
    private val demoPrompts = listOf(
        "Hello Kumaru, what is your status?",
        "Who are you and what can you do?",
        "Enna Kumaru, what time is it?",
        "Can you explain the Kumaru architecture?"
    )
    private var promptIndex = 0

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        _isListening = true
        resultCallback = onResult
        errorCallback = onError
    }

    override fun stopListening() {
        if (!_isListening) return
        _isListening = false

        val prompt = demoPrompts[promptIndex % demoPrompts.size]
        promptIndex++
        resultCallback?.invoke(prompt)
        resultCallback = null
        errorCallback = null
    }

    /**
     * Allows dispatching an explicit user input string while honoring the VoiceInput contract.
     */
    fun submitTranscript(text: String) {
        _isListening = false
        resultCallback?.invoke(text)
        resultCallback = null
        errorCallback = null
    }
}
