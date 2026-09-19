package com.kumaru.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.MessageRole
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.data.ai.AiProviderFactory
import com.kumaru.assistant.data.memory.InMemoryMemoryStore
import com.kumaru.assistant.data.tools.DefaultToolExecutor
import com.kumaru.assistant.data.voice.PushToTalkVoiceInput
import com.kumaru.assistant.data.voice.SystemVoiceOutput
import com.kumaru.assistant.domain.ai.AiProvider
import com.kumaru.assistant.domain.memory.MemoryStore
import com.kumaru.assistant.domain.tools.ToolExecutor
import com.kumaru.assistant.domain.voice.VoiceInput
import com.kumaru.assistant.domain.voice.VoiceOutput
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Core coordinator managing the Kumaru assistant state machine:
 * IDLE -> LISTENING -> THINKING -> SPEAKING -> IDLE
 *
 * Coordinates real Android SpeechRecognizer input, multi-turn Gemini reasoning,
 * and native TextToSpeech vocal playback.
 */
class AssistantViewModel(
    private val memoryStore: MemoryStore = InMemoryMemoryStore(),
    private val aiProvider: AiProvider = AiProviderFactory.create(memoryStore),
    private val voiceInput: VoiceInput = PushToTalkVoiceInput(),
    private val voiceOutput: VoiceOutput = SystemVoiceOutput(),
    private val toolExecutor: ToolExecutor = DefaultToolExecutor()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private var activeProcessingJob: Job? = null

    /**
     * Primary handler for the central Talk button.
     *
     * @param hasRecordPermission True if RECORD_AUDIO permission is granted.
     */
    fun onTalkButtonClicked(hasRecordPermission: Boolean = true) {
        when (_uiState.value.assistantState) {
            AssistantState.IDLE, AssistantState.ERROR -> {
                if (hasRecordPermission) {
                    startListening()
                } else {
                    onPermissionDenied()
                }
            }
            AssistantState.LISTENING -> {
                // User finished speaking; conclude capture
                voiceInput.stopListening()
            }
            AssistantState.THINKING -> {
                // Currently reasoning, wait for completion
            }
            AssistantState.SPEAKING -> {
                // User interrupted the assistant's speech
                interruptSpeaking()
            }
        }
    }

    /**
     * Invoked when runtime microphone permission is denied.
     */
    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                assistantState = AssistantState.ERROR,
                errorMessage = "Microphone permission is required for voice input. Please allow it to speak with Kumaru."
            )
        }
    }

    /**
     * Directly triggers an interaction with predefined query suggestions.
     */
    fun onSuggestionSelected(query: String) {
        if (_uiState.value.assistantState == AssistantState.THINKING) {
            return // Reject repeated triggers while reasoning
        }
        if (_uiState.value.assistantState == AssistantState.SPEAKING) {
            interruptSpeaking()
        }
        processUserInput(query)
    }

    private fun startListening() {
        _uiState.update {
            it.copy(
                assistantState = AssistantState.LISTENING,
                currentInput = "",
                errorMessage = null
            )
        }

        voiceInput.startListening(
            onResult = { transcript ->
                _uiState.update { it.copy(currentInput = "") }
                processUserInput(transcript)
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.ERROR,
                        errorMessage = error.localizedMessage ?: "Voice input error occurred",
                        currentInput = ""
                    )
                }
            },
            onPartialResult = { partialTranscript ->
                _uiState.update {
                    it.copy(currentInput = partialTranscript)
                }
            }
        )
    }

    private fun processUserInput(input: String) {
        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            _uiState.update { it.copy(assistantState = AssistantState.IDLE) }
            return
        }

        activeProcessingJob?.cancel()
        activeProcessingJob = viewModelScope.launch {
            try {
                // 1. Record User Message
                val userMessage = ConversationMessage(
                    role = MessageRole.USER,
                    text = trimmedInput
                )
                memoryStore.saveMessage(userMessage)

                // 2. Transition to THINKING
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.THINKING,
                        messages = it.messages + userMessage,
                        currentInput = "",
                        errorMessage = null
                    )
                }

                // 3. Obtain AI response (Gemini REST API with session context)
                val responseText = aiProvider.generateResponse(trimmedInput)

                // 4. Record Assistant Message
                val assistantMessage = ConversationMessage(
                    role = MessageRole.KUMARU,
                    text = responseText
                )
                memoryStore.saveMessage(assistantMessage)

                // 5. Transition to SPEAKING
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.SPEAKING,
                        messages = it.messages + assistantMessage
                    )
                }

                // 6. Voice output synthesis via native TextToSpeech
                voiceOutput.speak(responseText)

                // 7. Transition back to IDLE
                _uiState.update {
                    it.copy(assistantState = AssistantState.IDLE)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.ERROR,
                        errorMessage = e.localizedMessage ?: "Processing error occurred"
                    )
                }
            }
        }
    }

    private fun interruptSpeaking() {
        voiceOutput.stop()
        activeProcessingJob?.cancel()
        _uiState.update { it.copy(assistantState = AssistantState.IDLE) }
    }

    /**
     * Clears all session messages and resets state to IDLE.
     */
    fun resetConversation() {
        interruptSpeaking()
        viewModelScope.launch {
            memoryStore.clearHistory()
            _uiState.update {
                AssistantUiState(
                    assistantState = AssistantState.IDLE,
                    messages = emptyList(),
                    currentInput = "",
                    errorMessage = null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceOutput.stop()
        voiceOutput.release()
        if (voiceInput.isListening) {
            voiceInput.stopListening()
        }
        voiceInput.destroy()
    }
}

