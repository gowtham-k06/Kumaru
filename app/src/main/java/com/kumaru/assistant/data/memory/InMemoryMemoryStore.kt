package com.kumaru.assistant.data.memory

import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.domain.memory.MemoryStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Ephemeral in-memory implementation of [MemoryStore] for V0.1.
 * Provides thread-safe conversational context buffer during the active session.
 */
class InMemoryMemoryStore : MemoryStore {

    private val mutex = Mutex()
    private val buffer = mutableListOf<ConversationMessage>()

    override suspend fun saveMessage(message: ConversationMessage) {
        mutex.withLock {
            buffer.add(message)
        }
    }

    override suspend fun getRecentMessages(limit: Int): List<ConversationMessage> {
        return mutex.withLock {
            buffer.takeLast(limit)
        }
    }

    override suspend fun clearHistory() {
        mutex.withLock {
            buffer.clear()
        }
    }
}
