package com.kumaru.assistant.core.model

import java.util.UUID

/**
 * Conceptual memory ownership model for Kumaru.
 * Supports distinct personal vaults for Gowtham and Pavi, plus shared memories.
 */
enum class MemoryOwner {
    PERSONAL_GOWTHAM,
    PERSONAL_PAVI,
    SHARED;

    companion object {
        fun fromUserIdentity(identity: UserIdentity): MemoryOwner = when (identity) {
            UserIdentity.GOWTHAM -> PERSONAL_GOWTHAM
            UserIdentity.PAVI -> PERSONAL_PAVI
        }
    }
}

/**
 * Controlled, extensible set of memory categories.
 */
enum class MemoryCategory {
    PREFERENCE,
    PERSONAL_INFO,
    HABIT,
    INTEREST,
    IMPORTANT_DATE,
    RELATIONSHIP,
    SHARED_MEMORY,
    GOAL,
    ROUTINE,
    CONVERSATION_PREFERENCE,
    OTHER
}

/**
 * Conceptual memory tiers/levels for Kumaru.
 */
enum class MemoryLevel {
    SESSION,        // Ephemeral in-session message history
    RECENT,         // Recent key conversation topics and context summaries
    PERSONAL,       // User-specific persistent facts, traits, and preferences
    SHARED,         // Important shared memories involving both Gowtham & Pavi
    RELATIONSHIP    // Stable interaction patterns, inside jokes, and recurring themes
}

/**
 * Source attribution for how a memory was acquired.
 */
enum class MemorySource {
    USER_EXPLICIT,    // "Remember that...", "Don't forget..."
    USER_STATED,      // Stated preference/fact in conversation
    INFERRED          // Contextually deduced (lower initial confidence)
}

/**
 * Structured persistent memory item stored locally.
 */
data class MemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val owner: MemoryOwner,
    val category: MemoryCategory,
    val content: String,
    val key: String = "",
    val importance: Int = 3, // 1..5 scale (5 = highest)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val source: MemorySource = MemorySource.USER_STATED,
    val confidence: Float = 1.0f, // 0.0 to 1.0
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
) {
    val level: MemoryLevel
        get() = when (owner) {
            MemoryOwner.PERSONAL_GOWTHAM, MemoryOwner.PERSONAL_PAVI -> MemoryLevel.PERSONAL
            MemoryOwner.SHARED -> if (category == MemoryCategory.RELATIONSHIP) MemoryLevel.RELATIONSHIP else MemoryLevel.SHARED
        }

    val targetUser: UserIdentity?
        get() = when (owner) {
            MemoryOwner.PERSONAL_GOWTHAM -> UserIdentity.GOWTHAM
            MemoryOwner.PERSONAL_PAVI -> UserIdentity.PAVI
            MemoryOwner.SHARED -> null
        }
}
