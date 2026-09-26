package com.kumaru.assistant.data.memory

import android.content.Context
import android.content.SharedPreferences
import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.MemoryCategory
import com.kumaru.assistant.core.model.MemoryItem
import com.kumaru.assistant.core.model.MemoryOwner
import com.kumaru.assistant.core.model.MemorySource
import com.kumaru.assistant.core.model.UserIdentity
import com.kumaru.assistant.domain.memory.MemoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local-first, privacy-respecting, persistent implementation of [MemoryStore].
 *
 * Persists structured memories locally to device private storage.
 * Zero external calls, zero cloud telemetry, 100% private.
 *
 * Survives:
 * - App restart
 * - Process death
 * - Screen navigation
 */
class PersistentMemoryStore(context: Context) : MemoryStore {

    companion object {
        private const val PREFS_NAME = "kumaru_persistent_memories"
        private const val KEY_MEMORIES = "stored_memories_v1"

        @Volatile
        private var INSTANCE: PersistentMemoryStore? = null

        fun getInstance(context: Context): PersistentMemoryStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PersistentMemoryStore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val mutex = Mutex()
    private val sessionBuffer = mutableListOf<ConversationMessage>()

    private val _memoriesFlow = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memoriesFlow: StateFlow<List<MemoryItem>> = _memoriesFlow.asStateFlow()

    init {
        loadMemoriesFromDisk()
    }

    private fun loadMemoriesFromDisk() {
        val jsonStr = prefs.getString(KEY_MEMORIES, null)
        if (jsonStr.isNullOrBlank()) {
            val initial = createFoundationalMemories()
            persistMemoriesToDisk(initial)
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<MemoryItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val ownerStr = obj.optString("owner", MemoryOwner.SHARED.name)
                    val categoryStr = obj.optString("category", MemoryCategory.OTHER.name)
                    val content = obj.getString("content")
                    val key = obj.optString("key", "")
                    val importance = obj.optInt("importance", 3)
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    val sourceStr = obj.optString("source", MemorySource.USER_STATED.name)
                    val confidence = obj.optDouble("confidence", 1.0).toFloat()
                    val isPinned = obj.optBoolean("isPinned", false)
                    val isArchived = obj.optBoolean("isArchived", false)

                    val owner = try { MemoryOwner.valueOf(ownerStr) } catch (_: Exception) { MemoryOwner.SHARED }
                    val category = try { MemoryCategory.valueOf(categoryStr) } catch (_: Exception) { MemoryCategory.OTHER }
                    val source = try { MemorySource.valueOf(sourceStr) } catch (_: Exception) { MemorySource.USER_STATED }

                    list.add(
                        MemoryItem(
                            id = id,
                            owner = owner,
                            category = category,
                            content = content,
                            key = key,
                            importance = importance,
                            createdAt = createdAt,
                            updatedAt = updatedAt,
                            source = source,
                            confidence = confidence,
                            isPinned = isPinned,
                            isArchived = isArchived
                        )
                    )
                }
                _memoriesFlow.value = list
            } catch (_: Exception) {
                val initial = createFoundationalMemories()
                persistMemoriesToDisk(initial)
            }
        }
    }

    private fun createFoundationalMemories(): List<MemoryItem> {
        return listOf(
            // Gowtham
            MemoryItem(
                owner = MemoryOwner.PERSONAL_GOWTHAM,
                category = MemoryCategory.HABIT,
                key = "profession_habits",
                content = "Software engineer building Kumaru. Known to do deep technical work late into the night.",
                importance = 4,
                source = MemorySource.USER_STATED
            ),
            MemoryItem(
                owner = MemoryOwner.PERSONAL_GOWTHAM,
                category = MemoryCategory.PREFERENCE,
                key = "preferences",
                content = "Prefers mountains over beach; enjoys analytical and philosophical questions; values direct authenticity.",
                importance = 3,
                source = MemorySource.USER_STATED
            ),
            // Pavi
            MemoryItem(
                owner = MemoryOwner.PERSONAL_PAVI,
                category = MemoryCategory.PERSONAL_INFO,
                key = "identity_vibe",
                content = "Known as 'Pavs'. Expressive, sharp-witted, empathetic; appreciates thoughtful check-ins and playful teasing.",
                importance = 4,
                source = MemorySource.USER_STATED
            ),
            MemoryItem(
                owner = MemoryOwner.PERSONAL_PAVI,
                category = MemoryCategory.PREFERENCE,
                key = "preferences",
                content = "Loves beach and open coastal vibes; enthusiastic participant and initiator of rapid question games.",
                importance = 3,
                source = MemorySource.USER_STATED
            ),
            // Shared
            MemoryItem(
                owner = MemoryOwner.SHARED,
                category = MemoryCategory.SHARED_MEMORY,
                key = "rapid_questions_game",
                content = "A recurring game starting with 'Rapid quick questions?' where both alternate asking each other quick, thoughtful, or quirky questions.",
                importance = 5,
                source = MemorySource.USER_STATED,
                isPinned = true
            ),
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

    private fun persistMemoriesToDisk(list: List<MemoryItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("owner", item.owner.name)
                put("category", item.category.name)
                put("content", item.content)
                put("key", item.key)
                put("importance", item.importance)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
                put("source", item.source.name)
                put("confidence", item.confidence.toDouble())
                put("isPinned", item.isPinned)
                put("isArchived", item.isArchived)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_MEMORIES, array.toString()).apply()
        _memoriesFlow.value = list
    }

    // ==========================================
    // Session Messages
    // ==========================================

    override suspend fun saveMessage(message: ConversationMessage) {
        mutex.withLock {
            sessionBuffer.add(message)
        }
    }

    override suspend fun getRecentMessages(limit: Int): List<ConversationMessage> {
        return mutex.withLock {
            sessionBuffer.takeLast(limit)
        }
    }

    override suspend fun clearHistory() {
        mutex.withLock {
            sessionBuffer.clear()
        }
    }

    // ==========================================
    // Structured Memory Operations
    // ==========================================

    override suspend fun addMemory(item: MemoryItem) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _memoriesFlow.value.toMutableList()
            val existingIndex = current.indexOfFirst {
                it.id == item.id || (it.owner == item.owner && it.key.isNotBlank() && it.key.equals(item.key, ignoreCase = true))
            }
            if (existingIndex >= 0) {
                current[existingIndex] = item.copy(updatedAt = System.currentTimeMillis())
            } else {
                current.add(0, item)
            }
            persistMemoriesToDisk(current)
        }
    }

    override suspend fun updateMemory(item: MemoryItem) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _memoriesFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == item.id }
            if (index >= 0) {
                current[index] = item.copy(updatedAt = System.currentTimeMillis())
                persistMemoriesToDisk(current)
            } else {
                current.add(0, item)
                persistMemoriesToDisk(current)
            }
        }
    }

    override suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _memoriesFlow.value.toMutableList()
            val removed = current.removeAll { it.id == id }
            if (removed) {
                persistMemoriesToDisk(current)
            }
        }
    }

    override suspend fun getMemory(id: String): MemoryItem? {
        return _memoriesFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun searchMemories(query: String): List<MemoryItem> {
        val q = query.lowercase().trim()
        val all = _memoriesFlow.value.filter { !it.isArchived }
        if (q.isEmpty()) return all
        return all.filter {
            it.content.lowercase().contains(q) || it.key.lowercase().contains(q)
        }
    }

    override suspend fun getMemoriesForOwner(owner: MemoryOwner): List<MemoryItem> {
        return _memoriesFlow.value.filter { it.owner == owner && !it.isArchived }
    }

    override suspend fun getSharedMemories(): List<MemoryItem> {
        return _memoriesFlow.value.filter { it.owner == MemoryOwner.SHARED && !it.isArchived }
    }

    override suspend fun getAllMemories(): List<MemoryItem> {
        return _memoriesFlow.value
    }

    override suspend fun pinMemory(id: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _memoriesFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                current[index] = current[index].copy(isPinned = isPinned, updatedAt = System.currentTimeMillis())
                persistMemoriesToDisk(current)
            }
        }
    }

    override suspend fun archiveMemory(id: String, isArchived: Boolean) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _memoriesFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                current[index] = current[index].copy(isArchived = isArchived, updatedAt = System.currentTimeMillis())
                persistMemoriesToDisk(current)
            }
        }
    }

    override suspend fun getRelevantMemories(query: String, user: UserIdentity, limit: Int): List<MemoryItem> {
        val normalizedQuery = query.lowercase()
        val all = _memoriesFlow.value.filter { !it.isArchived }

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

        return scored.sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
}
