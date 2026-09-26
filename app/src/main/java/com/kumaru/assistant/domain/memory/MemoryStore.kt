package com.kumaru.assistant.domain.memory

import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.MemoryCategory
import com.kumaru.assistant.core.model.MemoryItem
import com.kumaru.assistant.core.model.MemoryOwner
import com.kumaru.assistant.core.model.UserIdentity

/**
 * Abstraction for short-term session memory and structured long-term persistent memory.
 *
 * Supports:
 * - Session turns (ephemeral in-conversation stream)
 * - Structured multi-tier memory (PERSONAL_GOWTHAM, PERSONAL_PAVI, SHARED)
 * - Category filtering, keyword relevance matching, pinning, and deduplication
 */
interface MemoryStore {

    // ==========================================
    // Session Message Operations (Ephemeral)
    // ==========================================

    suspend fun saveMessage(message: ConversationMessage)

    suspend fun getRecentMessages(limit: Int = 20): List<ConversationMessage>

    suspend fun clearHistory()

    // ==========================================
    // Structured Memory Operations (Persistent)
    // ==========================================

    suspend fun addMemory(item: MemoryItem)

    suspend fun updateMemory(item: MemoryItem)

    suspend fun deleteMemory(id: String)

    suspend fun getMemory(id: String): MemoryItem?

    suspend fun searchMemories(query: String): List<MemoryItem>

    suspend fun getMemoriesForOwner(owner: MemoryOwner): List<MemoryItem>

    suspend fun getSharedMemories(): List<MemoryItem>

    suspend fun getAllMemories(): List<MemoryItem>

    suspend fun pinMemory(id: String, isPinned: Boolean)

    suspend fun archiveMemory(id: String, isArchived: Boolean)

    suspend fun getRelevantMemories(query: String, user: UserIdentity, limit: Int = 5): List<MemoryItem>

    // Compatibility helpers
    suspend fun saveMemory(item: MemoryItem) {
        addMemory(item)
    }

    suspend fun getMemories(category: MemoryCategory? = null, user: UserIdentity? = null): List<MemoryItem> {
        val all = getAllMemories()
        return all.filter { item ->
            val catMatch = category == null || item.category == category
            val userMatch = user == null || item.targetUser == null || item.targetUser == user
            catMatch && userMatch
        }
    }
}
