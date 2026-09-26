package com.kumaru.assistant.core.model

import java.util.UUID

enum class MessageRole {
    USER,
    KUMARU,
    SYSTEM
}

/**
 * Represents a single message within the conversation stream.
 * Retains user identity for multi-user session context.
 */
data class ConversationMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userIdentity: UserIdentity? = null
)
