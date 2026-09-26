package com.kumaru.assistant.domain.memory

import com.kumaru.assistant.core.model.MemoryCategory
import com.kumaru.assistant.core.model.MemoryItem
import com.kumaru.assistant.core.model.MemoryOwner
import com.kumaru.assistant.core.model.MemorySource
import com.kumaru.assistant.core.model.UserIdentity

/**
 * Result of memory command extraction.
 */
sealed class MemoryCommand {
    data class ExplicitRemember(
        val memoryItem: MemoryItem,
        val humanTopic: String
    ) : MemoryCommand()

    data class ForgetMemory(
        val targetQuery: String,
        val targetOwner: MemoryOwner
    ) : MemoryCommand()

    data class QueryMemories(
        val targetOwner: MemoryOwner,
        val promptUser: UserIdentity
    ) : MemoryCommand()

    object None : MemoryCommand()
}

/**
 * High-signal, local rule-based extractor for memory commands and intent.
 *
 * Distinguishes:
 * - Explicit user instructions ("Remember that...", "Don't forget...", "Keep in mind...")
 * - Explicit deletion/forgetting instructions ("Forget that...", "Delete that memory...")
 * - Memory querying ("What do you remember about me?", "What do you know about Pavi?")
 * - Deduplication and normalization to prevent memory bloat.
 *
 * Normal conversation is NOT converted into permanent memories.
 */
class MemoryExtractor {

    companion object {
        private val EXPLICIT_REMEMBER_TRIGGERS = listOf(
            "remember that ",
            "remember this: ",
            "remember this ",
            "remember ",
            "keep this in mind: ",
            "keep in mind that ",
            "keep in mind ",
            "don't forget that ",
            "dont forget that ",
            "don't forget ",
            "dont forget ",
            "save this: ",
            "save this ",
            "save that ",
            "from now on, ",
            "from now on ",
            "make sure to remember "
        )

        private val FORGET_TRIGGERS = listOf(
            "forget that ",
            "forget about ",
            "forget ",
            "delete that memory",
            "delete memory about ",
            "delete memory ",
            "remove that memory",
            "remove memory about ",
            "erase that memory",
            "erase "
        )

        private val QUERY_TRIGGERS = listOf(
            "what do you remember about me",
            "show me what you remember",
            "what do you remember",
            "what do you know about me",
            "tell me what you remember",
            "show my memories",
            "show me my memories"
        )
    }

    /**
     * Analyzes incoming user text to determine if it is an explicit memory command.
     */
    fun extractCommand(input: String, currentUser: UserIdentity): MemoryCommand {
        val lower = input.trim().lowercase()

        // 1. Check for Memory Query Commands
        if (isQueryCommand(lower)) {
            val targetOwner = when {
                lower.contains("pavi") || lower.contains("pavs") -> MemoryOwner.PERSONAL_PAVI
                lower.contains("gowtham") -> MemoryOwner.PERSONAL_GOWTHAM
                lower.contains("shared") || lower.contains("both of us") || lower.contains("our") -> MemoryOwner.SHARED
                else -> MemoryOwner.fromUserIdentity(currentUser)
            }
            return MemoryCommand.QueryMemories(targetOwner = targetOwner, promptUser = currentUser)
        }

        // Specific queries like "what does pavi like?" or "what does gowtham like?"
        if (lower.startsWith("what does pavi like") || lower.startsWith("what do you know about pavi")) {
            return MemoryCommand.QueryMemories(targetOwner = MemoryOwner.PERSONAL_PAVI, promptUser = currentUser)
        }
        if (lower.startsWith("what does gowtham like") || lower.startsWith("what do you know about gowtham")) {
            return MemoryCommand.QueryMemories(targetOwner = MemoryOwner.PERSONAL_GOWTHAM, promptUser = currentUser)
        }
        if (lower.contains("shared memories") || lower.contains("memories we have") || lower.contains("memories of us")) {
            return MemoryCommand.QueryMemories(targetOwner = MemoryOwner.SHARED, promptUser = currentUser)
        }

        // 2. Check for Forget / Delete Commands
        for (trigger in FORGET_TRIGGERS) {
            val idx = lower.indexOf(trigger)
            if (idx >= 0) {
                val query = lower.substring(idx + trigger.length).trim().removeSuffix(".")
                val targetOwner = when {
                    query.contains("pavi") -> MemoryOwner.PERSONAL_PAVI
                    query.contains("gowtham") -> MemoryOwner.PERSONAL_GOWTHAM
                    query.contains("shared") || query.contains("our") -> MemoryOwner.SHARED
                    else -> MemoryOwner.fromUserIdentity(currentUser)
                }
                return MemoryCommand.ForgetMemory(targetQuery = query, targetOwner = targetOwner)
            }
        }

        // 3. Check for Explicit Remember Commands
        for (trigger in EXPLICIT_REMEMBER_TRIGGERS) {
            if (lower.startsWith(trigger)) {
                val rawContent = input.trim().substring(trigger.length).trim().removeSuffix(".")
                if (rawContent.isNotBlank()) {
                    return buildExplicitRemember(rawContent, currentUser)
                }
            }
        }

        // 4. Strong directly stated preferences/goals ("I really hate pineapple", "My favourite anime is One Piece")
        val directPreference = extractDirectlyStatedFact(input.trim(), currentUser)
        if (directPreference != null) {
            return directPreference
        }

        return MemoryCommand.None
    }

    private fun isQueryCommand(lower: String): Boolean {
        for (trigger in QUERY_TRIGGERS) {
            if (lower.startsWith(trigger) || lower == trigger) return true
        }
        return false
    }

    private fun buildExplicitRemember(content: String, currentUser: UserIdentity): MemoryCommand.ExplicitRemember {
        val lower = content.lowercase()

        // Determine ownership
        val owner = when {
            lower.startsWith("our ") || lower.contains("we both") || lower.contains("both of us") || lower.contains("together") -> MemoryOwner.SHARED
            (currentUser == UserIdentity.GOWTHAM && (lower.contains("pavi") || lower.contains("pavs"))) -> MemoryOwner.PERSONAL_PAVI
            (currentUser == UserIdentity.PAVI && lower.contains("gowtham")) -> MemoryOwner.PERSONAL_GOWTHAM
            else -> MemoryOwner.fromUserIdentity(currentUser)
        }

        // Determine category & key
        val category = inferCategory(lower)
        val key = inferKey(lower)

        val memoryItem = MemoryItem(
            owner = owner,
            category = category,
            content = content.replaceFirstChar { it.uppercase() },
            key = key,
            importance = 4,
            source = MemorySource.USER_EXPLICIT,
            confidence = 1.0f
        )

        return MemoryCommand.ExplicitRemember(
            memoryItem = memoryItem,
            humanTopic = content
        )
    }

    /**
     * Detects strong directly stated facts (e.g. "I hate pineapple", "My favourite anime is...")
     */
    private fun extractDirectlyStatedFact(input: String, currentUser: UserIdentity): MemoryCommand.ExplicitRemember? {
        val lower = input.lowercase()

        val isStrongDislike = lower.startsWith("i hate ") || lower.startsWith("i dislike ") || lower.startsWith("i don't like ") || lower.startsWith("i dont like ")
        val isStrongLike = lower.startsWith("i love ") || lower.startsWith("my favourite ") || lower.startsWith("my favorite ")
        val isGoal = lower.startsWith("i want to become ") || lower.startsWith("i am trying to become ") || lower.startsWith("i'm trying to become ")

        if (isStrongDislike || isStrongLike || isGoal) {
            val owner = MemoryOwner.fromUserIdentity(currentUser)
            val category = when {
                isGoal -> MemoryCategory.GOAL
                lower.contains("anime") || lower.contains("movie") || lower.contains("film") || lower.contains("music") -> MemoryCategory.INTEREST
                else -> MemoryCategory.PREFERENCE
            }
            val key = inferKey(lower)
            val item = MemoryItem(
                owner = owner,
                category = category,
                content = input.replaceFirstChar { it.uppercase() },
                key = key,
                importance = 3,
                source = MemorySource.USER_STATED,
                confidence = 0.9f
            )
            return MemoryCommand.ExplicitRemember(memoryItem = item, humanTopic = input)
        }

        return null
    }

    private fun inferCategory(lower: String): MemoryCategory {
        return when {
            lower.contains("movie") || lower.contains("film") || lower.contains("anime") ||
                lower.contains("series") || lower.contains("music") || lower.contains("song") ||
                lower.contains("book") || lower.contains("game") -> MemoryCategory.INTEREST

            lower.contains("goal") || lower.contains("dream") || lower.contains("filmmaker") ||
                lower.contains("learn") || lower.contains("become") || lower.contains("aspire") -> MemoryCategory.GOAL

            lower.contains("birthday") || lower.contains("anniversary") || lower.contains("born") ||
                lower.contains("date") -> MemoryCategory.IMPORTANT_DATE

            lower.contains("routine") || lower.contains("habit") || lower.contains("every morning") ||
                lower.contains("every night") || lower.contains("usually") -> MemoryCategory.HABIT

            lower.contains("our ") || lower.contains("we ") || lower.contains("together") -> MemoryCategory.SHARED_MEMORY

            lower.contains("like") || lower.contains("love") || lower.contains("hate") ||
                lower.contains("prefer") || lower.contains("dislike") || lower.contains("favourite") ||
                lower.contains("favorite") -> MemoryCategory.PREFERENCE

            else -> MemoryCategory.PERSONAL_INFO
        }
    }

    private fun inferKey(lower: String): String {
        val keywords = listOf(
            "anime", "movie", "film", "series", "music", "song", "food", "coffee", "tea",
            "pineapple", "filmmaker", "director", "habit", "work", "beach", "mountains",
            "birthday", "book", "colour", "color", "place", "lilies", "flower"
        )
        for (kw in keywords) {
            if (lower.contains(kw)) {
                return kw
            }
        }
        return ""
    }

    /**
     * Deduplication helper: Finds an existing memory that matches the key or primary topic of [newMemory].
     */
    fun findMatchingMemory(existingMemories: List<MemoryItem>, newMemory: MemoryItem): MemoryItem? {
        val candidates = existingMemories.filter { it.owner == newMemory.owner && !it.isArchived }

        // 1. Direct non-blank key match
        if (newMemory.key.isNotBlank()) {
            val keyMatch = candidates.firstOrNull { it.key.equals(newMemory.key, ignoreCase = true) }
            if (keyMatch != null) return keyMatch
        }

        // 2. Substantive topic token overlap
        val stopWords = setOf(
            "remember", "that", "this", "from", "on", "the", "and", "about", "for", "with",
            "my", "your", "our", "is", "are", "love", "like", "hate", "dislike", "favourite",
            "favorite", "really", "want", "trying", "to", "become"
        )
        val newTokens = newMemory.content.lowercase()
            .split(" ", ",", ".", "!", "?", "'", "\"")
            .filter { it.length > 3 && it !in stopWords }

        if (newTokens.isEmpty()) return null

        return candidates.firstOrNull { existing ->
            val existingLower = (existing.key + " " + existing.content).lowercase()
            newTokens.any { token -> existingLower.contains(token) }
        }
    }
}
