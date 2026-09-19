package com.kumaru.assistant.domain.tools

/**
 * Definition of an executable capability available to Kumaru.
 *
 * Future planned tools include:
 * - [CommonTools.OPEN_APP]
 * - [CommonTools.SET_ALARM]
 * - [CommonTools.CREATE_REMINDER]
 * - [CommonTools.MAKE_CALL]
 * - [CommonTools.OPEN_BROWSER]
 * - [CommonTools.GET_WEATHER]
 * - [CommonTools.SEARCH_WEB]
 */
data class ToolDefinition(
    val name: String,
    val description: String,
    val requiredParameters: List<String> = emptyList()
)

sealed class ToolResult {
    data class Success(val output: String) : ToolResult()
    data class Failure(val errorMessage: String, val cause: Throwable? = null) : ToolResult()
}

object CommonTools {
    const val OPEN_APP = "openApp"
    const val SET_ALARM = "setAlarm"
    const val CREATE_REMINDER = "createReminder"
    const val MAKE_CALL = "makeCall"
    const val OPEN_BROWSER = "openBrowser"
    const val GET_WEATHER = "getWeather"
    const val SEARCH_WEB = "searchWeb"
}
