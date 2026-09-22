package com.kumaru.assistant.presentation.viewmodel

import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.data.prompts.SuggestedPrompt
import com.kumaru.assistant.data.prompts.SuggestedPromptRepository
import java.util.UUID

data class AssistantUiState(
    val assistantState: AssistantState = AssistantState.IDLE,
    val messages: List<ConversationMessage> = emptyList(),
    val currentInput: String = "",
    val errorMessage: String? = null,
    val activeSessionId: String = UUID.randomUUID().toString(),
    val suggestedPrompts: List<SuggestedPrompt> = SuggestedPromptRepository.getCuratedSuggestions(4)
)
