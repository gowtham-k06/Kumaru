package com.kumaru.assistant.domain.engine

import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.InteractionMode
import com.kumaru.assistant.core.model.KumaruResponse
import com.kumaru.assistant.core.model.MemoryOwner
import com.kumaru.assistant.core.model.UserIdentity
import com.kumaru.assistant.data.personality.DefaultPersonaProvider
import com.kumaru.assistant.data.personality.PersonaProvider
import com.kumaru.assistant.data.personality.RelationshipProfileData
import com.kumaru.assistant.data.tools.DefaultToolExecutor
import com.kumaru.assistant.domain.ai.AiProvider
import com.kumaru.assistant.domain.memory.MemoryCommand
import com.kumaru.assistant.domain.memory.MemoryExtractor
import com.kumaru.assistant.domain.memory.MemoryStore
import com.kumaru.assistant.domain.tools.CommonTools
import com.kumaru.assistant.domain.tools.ToolExecutor
import com.kumaru.assistant.domain.tools.ToolResult

/**
 * Domain-level orchestrator for Kumaru V0.2.6.
 * Coordinates user identity, personality profile, relationship context,
 * persistent structured memory engine, interaction mode detection, AI reasoning, and tool execution.
 *
 * Handles natural memory commands ("Remember that...", "Forget that...", "What do you remember?")
 * with personality-aligned responses, while ensuring normal conversational messages do not pollute
 * permanent memory.
 */
class KumaruEngine(
    private val aiProvider: AiProvider,
    private val memoryStore: MemoryStore,
    private val toolExecutor: ToolExecutor = DefaultToolExecutor(),
    private val personaProvider: PersonaProvider = DefaultPersonaProvider,
    private val contextBuilder: ContextBuilder = ContextBuilder(personaProvider),
    private val modeDetector: InteractionModeDetector = InteractionModeDetector(),
    private val memoryExtractor: MemoryExtractor = MemoryExtractor()
) {

    /**
     * Main entry point for user interaction reasoning.
     *
     * @param input Raw transcribed user text.
     * @param userIdentity Active user (GOWTHAM or PAVI).
     * @param recentHistory Recent conversational messages.
     * @return Fully contextualized [KumaruResponse] ready for presentation and voice synthesis.
     */
    suspend fun processMessage(
        input: String,
        userIdentity: UserIdentity,
        recentHistory: List<ConversationMessage> = emptyList()
    ): KumaruResponse {
        val trimmedInput = input.trim()

        // 1. Evaluate Memory Commands (Remember, Forget, Query)
        val memoryCommand = memoryExtractor.extractCommand(trimmedInput, userIdentity)
        when (memoryCommand) {
            is MemoryCommand.ExplicitRemember -> {
                val memoryItem = memoryCommand.memoryItem
                val allMemories = memoryStore.getAllMemories()
                val existing = memoryExtractor.findMatchingMemory(allMemories, memoryItem)
                if (existing != null) {
                    val updated = existing.copy(
                        content = memoryItem.content,
                        importance = maxOf(existing.importance, memoryItem.importance),
                        updatedAt = System.currentTimeMillis(),
                        source = memoryItem.source,
                        confidence = memoryItem.confidence
                    )
                    memoryStore.updateMemory(updated)
                } else {
                    memoryStore.addMemory(memoryItem)
                }

                val reply = when {
                    memoryItem.owner == MemoryOwner.SHARED -> {
                        if (userIdentity == UserIdentity.PAVI) {
                            "Got it Pavs! Saved that into our shared memories: ${memoryItem.content}."
                        } else {
                            "Got it da, that's permanently saved as a shared memory: ${memoryItem.content}."
                        }
                    }
                    userIdentity == UserIdentity.PAVI -> {
                        "Got it Pavs! I've noted that down for you: ${memoryItem.content}."
                    }
                    else -> {
                        "Got it da, saved that in mind: ${memoryItem.content}."
                    }
                }
                return KumaruResponse(
                    text = reply,
                    mode = InteractionMode.CASUAL,
                    toneHint = "warm and reassuring",
                    voiceProfile = userIdentity.voiceProfileHint
                )
            }

            is MemoryCommand.ForgetMemory -> {
                val allMemories = memoryStore.getAllMemories()
                val target = allMemories.firstOrNull {
                    it.owner == memoryCommand.targetOwner && !it.isArchived &&
                        (it.content.contains(memoryCommand.targetQuery, ignoreCase = true) ||
                            it.key.contains(memoryCommand.targetQuery, ignoreCase = true) ||
                            memoryCommand.targetQuery.contains(it.key, ignoreCase = true))
                }

                val reply = if (target != null) {
                    memoryStore.deleteMemory(target.id)
                    if (userIdentity == UserIdentity.PAVI) {
                        "Done Pavs! I've cleared that from my memory."
                    } else {
                        "Done da! Cleared that memory."
                    }
                } else {
                    if (userIdentity == UserIdentity.PAVI) {
                        "I checked, but didn't find any memory matching '${memoryCommand.targetQuery}', Pavs."
                    } else {
                        "I checked, but couldn't find an active memory matching '${memoryCommand.targetQuery}' da."
                    }
                }
                return KumaruResponse(
                    text = reply,
                    mode = InteractionMode.CASUAL,
                    toneHint = "direct and honest",
                    voiceProfile = userIdentity.voiceProfileHint
                )
            }

            is MemoryCommand.QueryMemories -> {
                val memories = memoryStore.getMemoriesForOwner(memoryCommand.targetOwner)
                val ownerDescription = when (memoryCommand.targetOwner) {
                    MemoryOwner.PERSONAL_GOWTHAM -> if (userIdentity == UserIdentity.GOWTHAM) "you" else "Gowtham"
                    MemoryOwner.PERSONAL_PAVI -> if (userIdentity == UserIdentity.PAVI) "you" else "Pavi"
                    MemoryOwner.SHARED -> "both of you"
                }
                val reply = if (memories.isEmpty()) {
                    if (memoryCommand.targetOwner == MemoryOwner.SHARED) {
                        "No shared memories saved yet! You can say 'Remember that our favourite place is...' anytime."
                    } else if (userIdentity == UserIdentity.PAVI) {
                        "I don't have any specific saved memories for $ownerDescription yet, Pavs."
                    } else {
                        "I don't have any specific saved memories for $ownerDescription yet da."
                    }
                } else {
                    val bullets = memories.take(6).joinToString("\n") { "• ${it.content}" }
                    if (userIdentity == UserIdentity.PAVI) {
                        "Here's what I have saved about $ownerDescription, Pavs:\n$bullets"
                    } else {
                        "Here's what I have saved about $ownerDescription da:\n$bullets"
                    }
                }
                return KumaruResponse(
                    text = reply,
                    mode = InteractionMode.CASUAL,
                    toneHint = "warm and conversational",
                    voiceProfile = userIdentity.voiceProfileHint
                )
            }

            is MemoryCommand.None -> {
                // Continue standard reasoning
            }
        }

        // 2. Detect conversational mode
        val mode = modeDetector.detectMode(trimmedInput, recentHistory)

        // 3. Retrieve relevant memories for this user and query
        val relevantMemories = memoryStore.getRelevantMemories(trimmedInput, userIdentity, limit = 4)

        // 4. Check for specific tool intent (alarms, timers, reminders, apps)
        if (mode == InteractionMode.TASK_FOCUSED) {
            val toolExecutionResponse = handlePotentialTool(trimmedInput, userIdentity, mode)
            if (toolExecutionResponse != null) {
                return toolExecutionResponse
            }
        }

        // 5. Construct compact, high-signal system instruction with relevant memories
        val systemInstruction = contextBuilder.buildSystemInstruction(
            userIdentity = userIdentity,
            mode = mode,
            relevantMemories = relevantMemories
        )

        // 6. Generate AI response via AiProvider
        val responseText = aiProvider.generateResponseWithContext(
            input = trimmedInput,
            systemContext = systemInstruction
        )

        // 7. Infer follow-up suggestion (especially for rapid questions)
        val suggestedFollowUp = if (mode == InteractionMode.QUICK_QUESTIONS) {
            RelationshipProfileData.rapidQuestionsPool.randomOrNull()
        } else {
            null
        }

        // 8. Assemble KumaruResponse with mode, tone hint, and voice profile hint
        val toneHint = when (mode) {
            InteractionMode.PLAYFUL -> "playful teasing"
            InteractionMode.QUICK_QUESTIONS -> "snappy alternating question"
            InteractionMode.SERIOUS -> "reflective and attentive"
            InteractionMode.SUPPORTIVE -> "empathetic and warm"
            InteractionMode.LATE_NIGHT -> "calm and gentle"
            InteractionMode.TASK_FOCUSED -> "direct and concise"
            InteractionMode.CASUAL -> "relaxed and conversational"
        }

        return KumaruResponse(
            text = responseText,
            mode = mode,
            toneHint = toneHint,
            voiceProfile = userIdentity.voiceProfileHint,
            suggestedFollowUp = suggestedFollowUp
        )
    }

    private suspend fun handlePotentialTool(
        input: String,
        userIdentity: UserIdentity,
        mode: InteractionMode
    ): KumaruResponse? {
        val lower = input.lowercase()
        // Check for alarm or reminder execution requests
        if (lower.contains("set an alarm") || lower.contains("set alarm") || lower.contains("alarm for")) {
            val result = toolExecutor.execute(CommonTools.SET_ALARM, mapOf("query" to input))
            val message = when (result) {
                is ToolResult.Success -> result.output
                is ToolResult.Failure -> {
                    if (userIdentity == UserIdentity.PAVI) {
                        "Pavs, I've got the alarm tool lined up for you, but device alarm access is coming in the next build."
                    } else {
                        "Gowtham, alarm integration is defined in the architecture; pending device permission hook in V0.3."
                    }
                }
            }
            return KumaruResponse(
                text = message,
                mode = mode,
                toneHint = "direct and honest",
                voiceProfile = userIdentity.voiceProfileHint
            )
        }
        return null
    }
}
