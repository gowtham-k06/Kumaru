package com.kumaru.assistant.data.memory

import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.MemoryCategory
import com.kumaru.assistant.core.model.MemoryItem
import com.kumaru.assistant.core.model.MemoryOwner
import com.kumaru.assistant.core.model.MemorySource
import com.kumaru.assistant.core.model.UserIdentity
import com.kumaru.assistant.domain.memory.MemoryStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory thread-safe implementation of [MemoryStore].
 * Useful for tests, previews, and fast mocking.
 */
class InMemoryMemoryStore : MemoryStore {

    private val mutex = Mutex()
    private val buffer = mutableListOf<ConversationMessage>()
    private val memoryItems = mutableListOf<MemoryItem>()

    init {
        seedFoundationalMemories()
    }

    private fun seedFoundationalMemories() {
        // PERSONAL - Gowtham
        memoryItems.add(
            MemoryItem(
                owner = MemoryOwner.PERSONAL_GOWTHAM,
                category = MemoryCategory.HABIT,
                key = "profession_habits",
                content = "Software engineer building Kumaru. Known to do deep technical work late into the night.",
                importance = 4,
                source = MemorySource.USER_STATED
            )
        )
        memoryItems.add(
            MemoryItem(
                owner = MemoryOwner.PERSONAL_GOWTHAM,
                category = MemoryCategory.PREFERENCE,
                key = "preferences",
                content = "Prefers mountains over beach; enjoys analytical and philosophical questions; values direct authenticity.",
                importance = 3,
                source = MemorySource.USER_STATED
            )
        )

        // PERSONAL - Pavi
        memoryItems.add(
            MemoryItem(
                owner = MemoryOwner.PERSONAL_PAVI,
                category = MemoryCategory.PERSONAL_INFO,
                key = "identity_vibe",
                content = "Known as 'Pavs'. Expressive, sharp-witted, empathetic; appreciates thoughtful check-ins and playful teasing.",
                importance = 4,
                source = MemorySource.USER_STATED
            )
        )
        memoryItems.add(
            MemoryItem(
                owner = MemoryOwner.PERSONAL_PAVI,
                category = MemoryCategory.PREFERENCE,
                key = "preferences",
                content = "Loves beach and open coastal vibes; enthusiastic participant and initiator of rapid question games.",
                importance = 3,
                source = MemorySource.USER_STATED
            )
        )

        // SHARED - Gowtham & Pavi
        memoryItems.add(
            MemoryItem(
                owner = MemoryOwner.SHARED,
                category = MemoryCategory.SHARED_MEMORY,
                key = "rapid_questions_game",
                content = "A recurring game starting with 'Rapid quick questions?' where both alternate asking each other quick, thoughtful, or quirky questions.",
                importance = 5,
                source = MemorySource.USER_STATED,
                isPinned = true
            )
        )
        memoryItems.add(
            MemoryItem(
                owner = MemoryOwner.SHARED,
                category = MemoryCategory.RELATIONSHIP,
                key = "shared_phrases",
                content = "Inside phrases: 'Vela mokkaya pogudha' (work check-in), 'Aama. Ellamee' (commiseration), 'Mountains or beach?', 'Namba vena onnu pannalam'.",
                importance = 4,
                source = MemorySource.USER_STATED
            )
        )
    }

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

    override suspend fun addMemory(item: MemoryItem) {
        mutex.withLock {
            val existingIndex = memoryItems.indexOfFirst {
                it.id == item.id || (it.owner == item.owner && it.key.isNotBlank() && it.key.equals(item.key, ignoreCase = true))
            }
            if (existingIndex >= 0) {
                memoryItems[existingIndex] = item.copy(updatedAt = System.currentTimeMillis())
            } else {
                memoryItems.add(0, item)
            }
        }
    }

    override suspend fun updateMemory(item: MemoryItem) {
        mutex.withLock {
            val index = memoryItems.indexOfFirst { it.id == item.id }
            if (index >= 0) {
                memoryItems[index] = item.copy(updatedAt = System.currentTimeMillis())
            } else {
                memoryItems.add(0, item)
            }
        }
    }

    override suspend fun deleteMemory(id: String) {
        mutex.withLock {
            memoryItems.removeAll { it.id == id }
        }
    }

    override suspend fun getMemory(id: String): MemoryItem? {
        return mutex.withLock {
            memoryItems.firstOrNull { it.id == id }
        }
    }

    override suspend fun searchMemories(query: String): List<MemoryItem> {
        return mutex.withLock {
            val q = query.lowercase().trim()
            if (q.isEmpty()) return@withLock memoryItems.toList()
            memoryItems.filter {
                it.content.lowercase().contains(q) || it.key.lowercase().contains(q)
            }
        }
    }

    override suspend fun getMemoriesForOwner(owner: MemoryOwner): List<MemoryItem> {
        return mutex.withLock {
            memoryItems.filter { it.owner == owner && !it.isArchived }
        }
    }

    override suspend fun getSharedMemories(): List<MemoryItem> {
        return mutex.withLock {
            memoryItems.filter { it.owner == MemoryOwner.SHARED && !it.isArchived }
        }
    }

    override suspend fun getAllMemories(): List<MemoryItem> {
        return mutex.withLock {
            memoryItems.toList()
        }
    }

    override suspend fun pinMemory(id: String, isPinned: Boolean) {
        mutex.withLock {
            val index = memoryItems.indexOfFirst { it.id == id }
            if (index >= 0) {
                val current = memoryItems[index]
                memoryItems[index] = current.copy(isPinned = isPinned, updatedAt = System.currentTimeMillis())
            }
        }
    }

    override suspend fun archiveMemory(id: String, isArchived: Boolean) {
        mutex.withLock {
            val index = memoryItems.indexOfFirst { it.id == id }
            if (index >= 0) {
                val current = memoryItems[index]
                memoryItems[index] = current.copy(isArchived = isArchived, updatedAt = System.currentTimeMillis())
            }
        }
    }

    override suspend fun getRelevantMemories(query: String, user: UserIdentity, limit: Int): List<MemoryItem> {
        return mutex.withLock {
            val normalizedQuery = query.lowercase()
            val all = memoryItems.filter { !it.isArchived }

            val candidates = all.filter { item ->
                when {
                    normalizedQuery.contains("pavi") || normalizedQuery.contains("pavs") -> {
                        item.owner == MemoryOwner.PERSONAL_PAVI || item.owner == MemoryOwner.SHARED
                    }
                    normalizedQuery.contains("gowtham") -> {
                        item.owner == MemoryOwner.PERSONAL_GOWTHAM || item.owner == MemoryOwner.SHARED
                    }
                    else -> {
                        val userOwner = MemoryOwner.fromUserIdentity(user)
                        item.owner == userOwner || item.owner == MemoryOwner.SHARED
                    }
                }
            }

            val tokens = normalizedQuery.split(" ", ",", ".", "?", "!", "-", "'", "\"", "/")
                .filter { it.length > 2 }

            // Score by token matches, pinned bonus, and importance
            val scored = candidates.map { item ->
                var score = item.importance
                if (item.isPinned) score += 5
                val text = (item.key + " " + item.content).lowercase()
                for (token in tokens) {
                    if (text.contains(token)) {
                        score += 4
                    }
                }
                Pair(item, score)
            }

            scored.sortedByDescending { it.second }
                .take(limit)
                .map { it.first }
        }
    }
}
