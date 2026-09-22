package com.kumaru.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kumaru.assistant.KumaruApplication
import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.ConversationSession
import com.kumaru.assistant.core.model.MessageRole
import com.kumaru.assistant.core.model.UserProfile
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.data.ai.AiProviderFactory
import com.kumaru.assistant.data.history.ConversationHistoryRepository
import com.kumaru.assistant.data.memory.InMemoryMemoryStore
import com.kumaru.assistant.data.profile.UserProfileRepository
import com.kumaru.assistant.data.prompts.SuggestedPromptRepository
import com.kumaru.assistant.data.tools.DefaultToolExecutor
import com.kumaru.assistant.data.voice.PushToTalkVoiceInput
import com.kumaru.assistant.data.voice.SystemVoiceOutput
import com.kumaru.assistant.domain.ai.AiProvider
import com.kumaru.assistant.domain.memory.MemoryStore
import com.kumaru.assistant.domain.tools.ToolExecutor
import com.kumaru.assistant.domain.voice.VoiceInput
import com.kumaru.assistant.domain.voice.VoiceOutput
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Core coordinator managing the Kumaru assistant state machine:
 * IDLE -> LISTENING -> THINKING -> SPEAKING -> IDLE
 *
 * Coordinates multi-segment voice input, 15-second inactivity timeouts,
 * text input, multi-turn Gemini reasoning, local session history, and personalization profile.
 */
class AssistantViewModel(
    private val memoryStore: MemoryStore = InMemoryMemoryStore(),
    private val userProfileRepository: UserProfileRepository = UserProfileRepository.getInstance(KumaruApplication.appContext),
    private val conversationHistoryRepository: ConversationHistoryRepository = ConversationHistoryRepository.getInstance(KumaruApplication.appContext),
    private val aiProvider: AiProvider = AiProviderFactory.create(
        memoryStore = memoryStore,
        userContextProvider = { userProfileRepository.getPersonalizationPromptContext() }
    ),
    private val voiceInput: VoiceInput = PushToTalkVoiceInput(),
    private val voiceOutput: VoiceOutput = SystemVoiceOutput(),
    private val toolExecutor: ToolExecutor = DefaultToolExecutor()
) : ViewModel() {

    companion object {
        private const val INACTIVITY_TIMEOUT_MS = 15_000L
    }

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    val userProfile: StateFlow<UserProfile> = userProfileRepository.userProfile
    val conversationSessions: StateFlow<List<ConversationSession>> = conversationHistoryRepository.sessions

    private var activeProcessingJob: Job? = null
    private var inactivityTimerJob: Job? = null

    /**
     * Primary handler for the central Talk button.
     *
     * @param hasRecordPermission True if RECORD_AUDIO permission is granted.
     */
    fun onTalkButtonClicked(hasRecordPermission: Boolean = true) {
        when (_uiState.value.assistantState) {
            AssistantState.IDLE -> {
                if (hasRecordPermission) {
                    startListening()
                } else {
                    onPermissionDenied()
                }
            }
            AssistantState.LISTENING -> {
                // User manually stopped listening; stop capture and submit transcript
                cancelInactivityTimer()
                voiceInput.stopListening()
            }
            AssistantState.THINKING, AssistantState.SPEAKING -> {
                // Button disabled during reasoning and speaking
            }
            AssistantState.ERROR -> {
                // Return safely to IDLE
                cancelInactivityTimer()
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.IDLE,
                        errorMessage = null,
                        currentInput = ""
                    )
                }
            }
        }
    }

    /**
     * Invoked when runtime microphone permission is denied.
     */
    fun onPermissionDenied() {
        cancelInactivityTimer()
        _uiState.update {
            it.copy(
                assistantState = AssistantState.ERROR,
                errorMessage = "Microphone permission is required for voice input. Please allow it to speak with Kumaru."
            )
        }
    }

    /**
     * Directly triggers an interaction from text input.
     */
    fun onSendTextMessage(text: String) {
        submitUserMessage(text)
    }

    /**
     * Directly triggers an interaction with predefined query suggestions.
     */
    fun onSuggestionSelected(query: String) {
        submitUserMessage(query)
    }

    /**
     * Instantly rotates the curated prompt suggestions with a smooth transition.
     */
    fun refreshSuggestedPrompts() {
        _uiState.update {
            it.copy(suggestedPrompts = SuggestedPromptRepository.getCuratedSuggestions(4))
        }
    }

    /**
     * Shared message pipeline for both Voice and Text input.
     *
     * User Input -> Memory Store -> Gemini Reasoning -> Memory Store -> Vocal TTS -> Session History -> IDLE
     */
    fun submitUserMessage(input: String) {
        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            _uiState.update { it.copy(assistantState = AssistantState.IDLE) }
            return
        }

        // Prevent duplicate submissions during active reasoning
        if (_uiState.value.assistantState == AssistantState.THINKING) {
            return
        }

        if (_uiState.value.assistantState == AssistantState.SPEAKING) {
            interruptSpeaking()
        }

        cancelInactivityTimer()
        activeProcessingJob?.cancel()

        activeProcessingJob = viewModelScope.launch {
            try {
                // 1. Record User Message
                val userMessage = ConversationMessage(
                    role = MessageRole.USER,
                    text = trimmedInput
                )
                memoryStore.saveMessage(userMessage)

                val updatedMessagesWithUser = _uiState.value.messages + userMessage

                // 2. Transition to THINKING
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.THINKING,
                        messages = updatedMessagesWithUser,
                        currentInput = "",
                        errorMessage = null
                    )
                }

                // 3. Obtain AI response (Gemini REST API with session context + user profile)
                val responseText = aiProvider.generateResponse(trimmedInput)

                // 4. Record Assistant Message
                val assistantMessage = ConversationMessage(
                    role = MessageRole.KUMARU,
                    text = responseText
                )
                memoryStore.saveMessage(assistantMessage)

                val finalMessages = updatedMessagesWithUser + assistantMessage

                // 5. Persist to Local Conversation History
                val sessionTitle = finalMessages.firstOrNull { it.role == MessageRole.USER }?.text?.take(40)
                    ?: "Conversation with Kumaru"
                val session = ConversationSession(
                    id = _uiState.value.activeSessionId,
                    title = sessionTitle,
                    updatedAt = System.currentTimeMillis(),
                    messages = finalMessages
                )
                conversationHistoryRepository.saveSession(session)

                // 6. Transition to SPEAKING
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.SPEAKING,
                        messages = finalMessages
                    )
                }

                // 7. Voice output synthesis via native TextToSpeech
                voiceOutput.speak(responseText)

                // 8. Transition back to IDLE
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

    private fun startListening() {
        cancelInactivityTimer()
        _uiState.update {
            it.copy(
                assistantState = AssistantState.LISTENING,
                currentInput = "",
                errorMessage = null
            )
        }

        resetInactivityTimer()

        voiceInput.startListening(
            onResult = { transcript ->
                cancelInactivityTimer()
                _uiState.update { it.copy(currentInput = "") }
                submitUserMessage(transcript)
            },
            onError = { error ->
                cancelInactivityTimer()
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
            },
            onNoSpeech = {
                cancelInactivityTimer()
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.IDLE,
                        currentInput = "",
                        errorMessage = "No speech detected."
                    )
                }
            },
            onActivityDetected = {
                resetInactivityTimer()
            }
        )
    }

    private fun resetInactivityTimer() {
        inactivityTimerJob?.cancel()
        inactivityTimerJob = viewModelScope.launch {
            delay(INACTIVITY_TIMEOUT_MS)
            if (_uiState.value.assistantState == AssistantState.LISTENING) {
                voiceInput.stopListening()
            }
        }
    }

    private fun cancelInactivityTimer() {
        inactivityTimerJob?.cancel()
        inactivityTimerJob = null
    }

    private fun interruptSpeaking() {
        voiceOutput.stop()
        activeProcessingJob?.cancel()
        _uiState.update { it.copy(assistantState = AssistantState.IDLE) }
    }

    /**
     * Loads an existing conversation session from History to resume chatting.
     */
    fun loadSession(session: ConversationSession) {
        cancelInactivityTimer()
        interruptSpeaking()
        viewModelScope.launch {
            memoryStore.clearHistory()
            for (msg in session.messages) {
                memoryStore.saveMessage(msg)
            }
            _uiState.update {
                it.copy(
                    assistantState = AssistantState.IDLE,
                    messages = session.messages,
                    activeSessionId = session.id,
                    currentInput = "",
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Clears all session messages and starts a brand new conversation session.
     */
    fun resetConversation() {
        cancelInactivityTimer()
        interruptSpeaking()
        viewModelScope.launch {
            memoryStore.clearHistory()
            _uiState.update {
                AssistantUiState(
                    assistantState = AssistantState.IDLE,
                    messages = emptyList(),
                    activeSessionId = UUID.randomUUID().toString(),
                    currentInput = "",
                    errorMessage = null,
                    suggestedPrompts = SuggestedPromptRepository.getCuratedSuggestions(4)
                )
            }
        }
    }

    /**
     * Deletes a conversation from history.
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            conversationHistoryRepository.deleteSession(sessionId)
            if (_uiState.value.activeSessionId == sessionId) {
                resetConversation()
            }
        }
    }

    /**
     * Updates user profile preferences.
     */
    fun saveUserProfile(profile: UserProfile) {
        userProfileRepository.saveProfile(profile)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        userProfileRepository.setOnboardingCompleted(completed)
    }

    override fun onCleared() {
        super.onCleared()
        cancelInactivityTimer()
        activeProcessingJob?.cancel()
        voiceOutput.stop()
        voiceOutput.release()
        if (voiceInput.isListening) {
            voiceInput.stopListening()
        }
        voiceInput.destroy()
    }
}
