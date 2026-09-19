package com.kumaru.assistant.data.ai

import android.util.Log
import com.kumaru.assistant.BuildConfig
import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.MessageRole
import com.kumaru.assistant.domain.ai.AiProvider
import com.kumaru.assistant.domain.memory.MemoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/**
 * Production-ready Google Gemini AI provider for Kumaru V0.2A.
 * Connects asynchronously to the Gemini REST API using clean Android primitives
 * without bloated third-party dependencies.
 *
 * Supports:
 * - Multi-turn conversational session context
 * - Kumaru's calm, concise, Thanglish-aware personality
 * - Robust network & HTTP error degradation
 * - Strict API key security (never logged, injected via BuildConfig)
 */
class GeminiAiProvider(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val model: String = BuildConfig.GEMINI_MODEL,
    private val memoryStore: MemoryStore? = null
) : AiProvider {

    companion object {
        private const val TAG = "GeminiAiProvider"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 25000

        private const val SYSTEM_INSTRUCTION =
            "You are Kumaru, the user's personal AI assistant on Android.\n\n" +
            "Personality & Guidelines:\n" +
            "- Natural, calm, intelligent, concise, slightly witty, and genuinely helpful.\n" +
            "- Conversational tone suitable for spoken interaction (short paragraphs, avoid heavy markdown tables or ASCII art).\n" +
            "- Do not be overly enthusiastic or sycophantic.\n" +
            "- Do not constantly repeat the user's name or end every sentence with 'How can I help?'.\n" +
            "- Understand casual English, colloquialisms, and common Tamil and Thanglish expressions (e.g., 'Enna Kumaru', 'Sollunga', 'Vanakkam', 'Epdi irukinga', 'Seri', 'Nandri'). Feel free to sprinkle friendly, natural Tamil/Thanglish touches when addressed in Thanglish.\n" +
            "- Strict capability honesty: You are currently running on Kumaru V0.2A. Phone tools, alarms, reminders, calls, smart home, and device automation are still being engineered for upcoming versions. Never claim to have taken actions or set reminders/alarms on the phone unless explicitly done. If asked to do something unsupported, acknowledge it politely and naturally (e.g. 'I'm still learning how to interact with your phone's alarms, but I'm being updated soon!').\n" +
            "- Keep answers brief and conversational unless the user specifically requests an in-depth breakdown."
    }

    override suspend fun generateResponse(input: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "Gemini API key is not configured. Please add GEMINI_API_KEY to local.properties."
        }

        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            return@withContext "I'm listening. How can I help you?"
        }

        try {
            val endpointUrl = "$BASE_URL/$model:generateContent?key=$apiKey"
            val requestPayload = buildRequestBody(trimmedInput)

            val url = URL(endpointUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("User-Agent", "Kumaru-Assistant/0.2.0")
            }

            // Send request body
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestPayload)
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseBody = if (statusCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } else {
                val errorStream = connection.errorStream
                if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream, Charsets.UTF_8)).use { reader ->
                        reader.readText()
                    }
                } else {
                    ""
                }
            }

            handleApiResponse(statusCode, responseBody)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network offline or unreachable host: ${e.message}")
            "I can't seem to connect to the internet right now. Please check your network connection."
        } catch (e: ConnectException) {
            Log.e(TAG, "Connection refused: ${e.message}")
            "Could not connect to the AI service. Please check your network connection."
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Network request timed out: ${e.message}")
            "The request took a bit too long to complete. Please try asking again."
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in Gemini inference: ${e.javaClass.simpleName} - ${e.message}")
            "I encountered an unexpected error while processing: ${e.localizedMessage ?: "Please try again."}"
        }
    }

    private suspend fun buildRequestBody(currentInput: String): String {
        val root = JSONObject()

        // 1. System Instruction
        val systemInstructionObj = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", SYSTEM_INSTRUCTION))
            }
            put("parts", partsArray)
        }
        root.put("system_instruction", systemInstructionObj)

        // 2. Multi-turn Conversation Contents
        val contentsArray = JSONArray()

        val recentMessages = memoryStore?.getRecentMessages(limit = 10) ?: emptyList()
        val formattedTurns = buildFormattedTurns(recentMessages, currentInput)

        for (turn in formattedTurns) {
            val turnObj = JSONObject().apply {
                put("role", turn.role)
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", turn.text))
                }
                put("parts", parts)
            }
            contentsArray.put(turnObj)
        }

        root.put("contents", contentsArray)

        // 3. Generation Config
        val generationConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("topK", 40)
            put("topP", 0.95)
            put("maxOutputTokens", 800)
        }
        root.put("generationConfig", generationConfig)

        return root.toString()
    }

    private data class FormattedTurn(val role: String, val text: String)

    private fun buildFormattedTurns(
        history: List<ConversationMessage>,
        currentInput: String
    ): List<FormattedTurn> {
        val result = mutableListOf<FormattedTurn>()

        for (msg in history) {
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.KUMARU -> "model"
                MessageRole.SYSTEM -> continue
            }
            val text = msg.text.trim()
            if (text.isEmpty()) continue

            // Ensure no adjacent duplicate roles to satisfy Gemini API schema
            if (result.isNotEmpty() && result.last().role == role) {
                val previous = result.removeAt(result.size - 1)
                result.add(FormattedTurn(role, "${previous.text}\n$text"))
            } else {
                result.add(FormattedTurn(role, text))
            }
        }

        // Ensure the conversation starts with a 'user' turn
        while (result.isNotEmpty() && result.first().role != "user") {
            result.removeAt(0)
        }

        // Ensure the current input is the final 'user' turn
        if (result.isEmpty() || result.last().role != "user" || result.last().text != currentInput) {
            if (result.isNotEmpty() && result.last().role == "user") {
                result.removeAt(result.size - 1)
            }
            result.add(FormattedTurn("user", currentInput))
        }

        return result
    }

    private fun handleApiResponse(statusCode: Int, responseBody: String): String {
        return when (statusCode) {
            200 -> parseSuccessfulResponse(responseBody)
            400 -> {
                Log.e(TAG, "HTTP 400 Bad Request: $responseBody")
                val errorMsg = extractErrorMessage(responseBody)
                if (errorMsg.contains("API key", ignoreCase = true) || errorMsg.contains("invalid", ignoreCase = true)) {
                    "Invalid Gemini API key. Please verify the key provided in local.properties."
                } else {
                    "Unable to process request with Gemini: $errorMsg"
                }
            }
            401, 403 -> {
                Log.e(TAG, "HTTP $statusCode Auth Error: $responseBody")
                "Gemini API authentication failed. Please check your API key in local.properties."
            }
            404 -> {
                Log.e(TAG, "HTTP 404 Model Not Found: $responseBody")
                "Configured model '$model' was not found or is unavailable."
            }
            429 -> {
                Log.e(TAG, "HTTP 429 Rate Limited: $responseBody")
                "I'm receiving too many requests right now. Please wait a moment and try again."
            }
            500, 502, 503, 504 -> {
                Log.e(TAG, "HTTP $statusCode Server Error: $responseBody")
                "Gemini services are temporarily unavailable. Please try again shortly."
            }
            else -> {
                Log.e(TAG, "HTTP $statusCode Unexpected Response: $responseBody")
                "Received HTTP $statusCode from AI service. Please try again."
            }
        }
    }

    private fun parseSuccessfulResponse(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            val candidates = json.optJSONArray("candidates")

            if (candidates == null || candidates.length() == 0) {
                val promptFeedback = json.optJSONObject("promptFeedback")
                val blockReason = promptFeedback?.optString("blockReason")
                return if (!blockReason.isNullOrBlank()) {
                    "I cannot respond to this query due to safety filtering ($blockReason)."
                } else {
                    "I wasn't able to formulate a response. Could you rephrase your question?"
                }
            }

            val firstCandidate = candidates.getJSONObject(0)
            val finishReason = firstCandidate.optString("finishReason", "")

            if (finishReason.equals("SAFETY", ignoreCase = true)) {
                return "I'm unable to discuss that topic due to content safety guidelines."
            }

            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts != null && parts.length() > 0) {
                val sb = StringBuilder()
                for (i in 0 until parts.length()) {
                    val partObj = parts.getJSONObject(i)
                    val text = partObj.optString("text", "")
                    sb.append(text)
                }
                val responseText = sb.toString().trim()
                if (responseText.isNotEmpty()) {
                    responseText
                } else {
                    "I received an empty response. Let's try that again."
                }
            } else {
                "I couldn't generate a clear response. Please try again."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response JSON: ${e.message}")
            "Error reading response from AI service."
        }
    }

    private fun extractErrorMessage(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val errorObj = json.optJSONObject("error")
            errorObj?.optString("message") ?: "Bad Request"
        } catch (_: Exception) {
            "Request processing error"
        }
    }
}
