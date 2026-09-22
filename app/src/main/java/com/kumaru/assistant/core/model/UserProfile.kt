package com.kumaru.assistant.core.model

import java.util.UUID

/**
 * Represents a saved conversation session for history tracking and resumption.
 */
data class ConversationSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<ConversationMessage> = emptyList()
)

/**
 * User Profile Model containing personalization preferences gathered during onboarding.
 */
data class UserProfile(
    val isOnboardingCompleted: Boolean = false,
    val userName: String = "",
    val communicationStyle: String = "CASUAL", // CASUAL, FOCUSED, PLAYFUL, BALANCED
    val primaryInterests: Set<String> = emptySet(), // IDEAS, WORK, LEARNING, LIFE, CONVERSATION, ORGANIZATION
    val personalContext: String = "",
    val memoryPreference: String = "USEFUL" // MINIMAL, USEFUL, PERSONAL
)
