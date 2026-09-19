package com.kumaru.assistant.domain.tools

/**
 * Abstraction for registering and executing capabilities/tools requested by the AI.
 */
interface ToolExecutor {
    /**
     * List of tools registered and available for invocation.
     */
    val availableTools: List<ToolDefinition>

    /**
     * Executes a requested tool by name with provided arguments.
     *
     * @param toolName Unique tool identifier (e.g. [CommonTools.OPEN_APP], [CommonTools.SET_ALARM])
     * @param params Key-value map of execution parameters.
     */
    suspend fun execute(toolName: String, params: Map<String, Any?> = emptyMap()): ToolResult
}
