package com.kumaru.assistant.domain.memory

import com.kumaru.assistant.core.model.ConversationMessage

/**
 * Abstraction for short-term and long-term memory management.
 * In future stages, vector storage or Room DB will back this contract.
 */
interface MemoryStore {
    /**
     * Stores a conversation message in memory.
     */
    suspend fun saveMessage(message: ConversationMessage)

    /**
     * Retrieves the most recent conversational turns.
     */
    suspend fun getRecentMessages(limit: Int = 20): List<ConversationMessage>

    /**
     * Clears conversational memory buffer.
     */
    suspend fun clearHistory()
}
