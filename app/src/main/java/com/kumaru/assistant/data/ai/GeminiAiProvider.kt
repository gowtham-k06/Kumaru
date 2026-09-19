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
 * Production Google Gemini AI provider for Kumaru V0.2.
 * Connects asynchronously to the Gemini REST API on Dispatchers.IO.
 *
 * Diagnostics:
 * - Logs HTTP status codes, network errors, and raw API error payloads in Logcat.
 * - Strict API key security: keys are NEVER logged or exposed.
 */
class GeminiAiProvider(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val model: String = BuildConfig.GEMINI_MODEL,
    private val memoryStore: MemoryStore? = null
) : AiProvider {

    init {
        Log.i(TAG, "KUMARU PROVIDER CONSTRUCTOR MODEL = $model")
    }

    companion object {
        private const val TAG = "GeminiAiProvider"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 25000

        private const val SYSTEM_INSTRUCTION =
            "You are Kumaru, the user's personal AI assistant on Android.\n\n" +
            "Personality & Spoken Interaction Guidelines:\n" +
            "- Natural, calm, intelligent, concise, slightly witty, and genuinely helpful.\n" +
            "- Always optimize responses for spoken audio feedback (short conversational paragraphs, avoid markdown tables, ASCII art, or long lists).\n" +
            "- Do not be overly enthusiastic or sycophantic.\n" +
            "- Understand casual English and common Tamil / Thanglish expressions (e.g., 'Enna Kumaru', 'Sollunga', 'Vanakkam', 'Epdi irukinga', 'Seri', 'Nandri'). Respond with natural warmth.\n" +
            "- Strict capability honesty: Device tools and phone automations are in active engineering for upcoming versions. Never claim to have performed actions on the phone unless explicitly supported.\n" +
            "- Keep answers brief and conversational unless the user specifically asks for deep detail."
    }

    override suspend fun generateResponse(input: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.e(TAG, "Gemini API key is blank or unconfigured in BuildConfig")
            return@withContext "Gemini authentication failed. Please check your API key."
        }

        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            return@withContext "I'm listening. How can I help you?"
        }

        try {
            val activeModel = if (model.isNotBlank()) model else "gemini-3.5-flash-lite"
            val endpointUrl = "$BASE_URL/$activeModel:generateContent?key=$apiKey"
            val requestPayload = buildRequestBody(trimmedInput)
            val sanitizedEndpoint = "$BASE_URL/$activeModel:generateContent"

            Log.i(TAG, "KUMARU GEMINI MODEL = $activeModel")
            Log.i(TAG, "KUMARU GEMINI URL = $sanitizedEndpoint")
            Log.d(TAG, "[GEMINI_REQUEST] Model: $activeModel | Endpoint: $sanitizedEndpoint | Payload bytes: ${requestPayload.toByteArray().size}")

            val url = URL(endpointUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("x-goog-api-key", apiKey)
                setRequestProperty("User-Agent", "Kumaru-Assistant/0.2.0")
            }

            // Write request body
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

            Log.d(TAG, "[GEMINI_RESPONSE] HTTP status: $statusCode | Response length: ${responseBody.length}")
            handleApiResponse(statusCode, responseBody, activeModel, sanitizedEndpoint)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "[GEMINI_NETWORK_ERROR] UnknownHostException: ${e.message}")
            "Can't reach Gemini. Check your internet connection."
        } catch (e: ConnectException) {
            Log.e(TAG, "[GEMINI_NETWORK_ERROR] ConnectException: ${e.message}")
            "Can't reach Gemini. Check your internet connection."
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "[GEMINI_NETWORK_ERROR] SocketTimeoutException: ${e.message}")
            "Can't reach Gemini. Check your internet connection."
        } catch (e: Exception) {
            Log.e(TAG, "[GEMINI_UNEXPECTED_ERROR] ${e.javaClass.simpleName}: ${e.message}", e)
            "Error: ${e.localizedMessage ?: "Unexpected failure"}"
        }
    }

    private suspend fun buildRequestBody(currentInput: String): String {
        val root = JSONObject()

        // 1. System Instruction (using standard camelCase systemInstruction)
        val systemInstructionObj = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", SYSTEM_INSTRUCTION))
            }
            put("parts", partsArray)
        }
        root.put("systemInstruction", systemInstructionObj)

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

            if (result.isNotEmpty() && result.last().role == role) {
                val previous = result.removeAt(result.size - 1)
                result.add(FormattedTurn(role, "${previous.text}\n$text"))
            } else {
                result.add(FormattedTurn(role, text))
            }
        }

        while (result.isNotEmpty() && result.first().role != "user") {
            result.removeAt(0)
        }

        if (result.isEmpty() || result.last().role != "user" || result.last().text != currentInput) {
            if (result.isNotEmpty() && result.last().role == "user") {
                result.removeAt(result.size - 1)
            }
            result.add(FormattedTurn("user", currentInput))
        }

        return result
    }

    private fun handleApiResponse(
        statusCode: Int,
        responseBody: String,
        modelName: String,
        endpoint: String
    ): String {
        if (statusCode == 200) {
            return parseSuccessfulResponse(responseBody)
        }

        val parsedError = parseApiError(responseBody)
        val apiErrorCode = parsedError.code.takeIf { it > 0 } ?: statusCode
        val apiErrorStatus = parsedError.status
        val apiErrorMessage = parsedError.message

        Log.e(
            TAG,
            "[GEMINI_DIAGNOSTICS] HTTP $statusCode | Model: $modelName | Endpoint: $endpoint | ErrorCode: $apiErrorCode | ErrorStatus: '$apiErrorStatus' | ErrorMessage: '$apiErrorMessage'"
        )

        return if (apiErrorMessage.isNotBlank() && apiErrorMessage != "Request error") {
            "[API Error $statusCode: $apiErrorStatus] $apiErrorMessage"
        } else {
            "[HTTP $statusCode] Gemini service returned an error."
        }
    }

    private data class ApiErrorDetails(val code: Int, val status: String, val message: String)

    private fun parseApiError(responseBody: String): ApiErrorDetails {
        return try {
            val json = JSONObject(responseBody)
            val errorObj = json.optJSONObject("error")
            if (errorObj != null) {
                val code = errorObj.optInt("code", 0)
                val status = errorObj.optString("status", "")
                val message = errorObj.optString("message", "")
                ApiErrorDetails(code, status, message)
            } else {
                ApiErrorDetails(0, "", responseBody.take(200))
            }
        } catch (_: Exception) {
            ApiErrorDetails(0, "", "Raw response: ${responseBody.take(200)}")
        }
    }

    private fun parseSuccessfulResponse(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            val candidates = json.optJSONArray("candidates")

            if (candidates == null || candidates.length() == 0) {
                val promptFeedback = json.optJSONObject("promptFeedback")
                val blockReason = promptFeedback?.optString("blockReason")
                Log.w(TAG, "[GEMINI_BLOCKED] No candidates returned. Block reason: $blockReason")
                return if (!blockReason.isNullOrBlank()) {
                    "Response blocked by safety filter: $blockReason"
                } else {
                    "Empty response returned by Gemini."
                }
            }

            val firstCandidate = candidates.getJSONObject(0)
            val finishReason = firstCandidate.optString("finishReason", "")

            if (finishReason.equals("SAFETY", ignoreCase = true)) {
                Log.w(TAG, "[GEMINI_SAFETY] Finish reason: SAFETY")
                return "Response blocked by content safety guidelines."
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
                    "Gemini returned an empty text part."
                }
            } else {
                Log.e(TAG, "[GEMINI_PARSE_ERROR] Response candidate has empty parts: $jsonString")
                "Gemini returned an empty structure."
            }
        } catch (e: Exception) {
            Log.e(TAG, "[GEMINI_PARSE_ERROR] Failed to parse JSON response: ${e.message}", e)
            "Failed to parse Gemini response."
        }
    }
}

