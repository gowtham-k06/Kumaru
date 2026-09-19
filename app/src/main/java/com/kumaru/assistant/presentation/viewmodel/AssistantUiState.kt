package com.kumaru.assistant.presentation.viewmodel

import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.state.AssistantState

data class AssistantUiState(
    val assistantState: AssistantState = AssistantState.IDLE,
    val messages: List<ConversationMessage> = emptyList(),
    val currentInput: String = "",
    val errorMessage: String? = null
)
