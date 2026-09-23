package com.kumaru.assistant.core.model

import java.util.UUID

/**
 * Conceptual memory tiers for Kumaru's local memory store.
 */
enum class MemoryCategory {
    SESSION,        // Ephemeral in-session message history
    RECENT,         // Recent key conversation topics and context summaries
    PERSONAL,       // User-specific persistent facts, traits, and preferences
    SHARED,         // Important shared memories involving both Gowtham & Pavi
    RELATIONSHIP    // Stable interaction patterns, inside jokes, and recurring themes
}

/**
 * Structured memory item stored locally.
 */
data class MemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val category: MemoryCategory,
    val targetUser: UserIdentity? = null, // null means applies to both (shared/relationship)
    val key: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1 // 1..5 scale
)
