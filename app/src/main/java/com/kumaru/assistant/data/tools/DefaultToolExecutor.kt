package com.kumaru.assistant.data.tools

import com.kumaru.assistant.domain.tools.CommonTools
import com.kumaru.assistant.domain.tools.ToolDefinition
import com.kumaru.assistant.domain.tools.ToolExecutor
import com.kumaru.assistant.domain.tools.ToolResult

/**
 * Registry and dispatcher for Kumaru device & cloud tools.
 *
 * For V0.1, the tool definitions are formalized here without pretending
 * to execute real phone actions. Real implementations will be added
 * incrementally with proper Android runtime permissions and intent handlers.
 */
class DefaultToolExecutor : ToolExecutor {

    override val availableTools: List<ToolDefinition> = listOf(
        ToolDefinition(
            name = CommonTools.OPEN_APP,
            description = "Launch an installed Android application by package or display name",
            requiredParameters = listOf("appName")
        ),
        ToolDefinition(
            name = CommonTools.SET_ALARM,
            description = "Set an Android system alarm for a given time",
            requiredParameters = listOf("hour", "minute")
        ),
        ToolDefinition(
            name = CommonTools.CREATE_REMINDER,
            description = "Create a reminder or calendar notification",
            requiredParameters = listOf("title", "timestamp")
        ),
        ToolDefinition(
            name = CommonTools.MAKE_CALL,
            description = "Initiate a phone call to a contact or phone number",
            requiredParameters = listOf("recipient")
        ),
        ToolDefinition(
            name = CommonTools.OPEN_BROWSER,
            description = "Open a given URL in the default Android web browser",
            requiredParameters = listOf("url")
        ),
        ToolDefinition(
            name = CommonTools.GET_WEATHER,
            description = "Retrieve current weather for the user's location",
            requiredParameters = emptyList()
        ),
        ToolDefinition(
            name = CommonTools.SEARCH_WEB,
            description = "Perform a real-time web query",
            requiredParameters = listOf("query")
        )
    )

    override suspend fun execute(toolName: String, params: Map<String, Any?>): ToolResult {
        val tool = availableTools.firstOrNull { it.name == toolName }
            ?: return ToolResult.Failure("Tool '$toolName' is not recognized by Kumaru.")

        // Per project requirements, avoid fake implementations that pretend real actions happened.
        return ToolResult.Failure("Tool '${tool.name}' is defined in Kumaru V0.1 architecture but awaits device integration.")
    }
}
