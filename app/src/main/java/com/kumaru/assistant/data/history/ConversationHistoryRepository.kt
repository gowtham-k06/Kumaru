package com.kumaru.assistant.data.history

import android.content.Context
import android.content.SharedPreferences
import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.ConversationSession
import com.kumaru.assistant.core.model.MessageRole
import com.kumaru.assistant.core.model.UserIdentity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local-first repository for persisting and managing conversation sessions.
 */
class ConversationHistoryRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "kumaru_conversation_history"
        private const val KEY_SESSIONS = "saved_sessions"

        @Volatile
        private var INSTANCE: ConversationHistoryRepository? = null

        fun getInstance(context: Context): ConversationHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConversationHistoryRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _sessions = MutableStateFlow<List<ConversationSession>>(emptyList())
    val sessions: StateFlow<List<ConversationSession>> = _sessions.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        val jsonStr = prefs.getString(KEY_SESSIONS, null) ?: "[]"
        try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ConversationSession>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())

                val messagesArray = obj.optJSONArray("messages") ?: JSONArray()
                val messages = mutableListOf<ConversationMessage>()
                for (j in 0 until messagesArray.length()) {
                    val mObj = messagesArray.getJSONObject(j)
                    val mId = mObj.getString("id")
                    val roleStr = mObj.getString("role")
                    val text = mObj.getString("text")
                    val timestamp = mObj.optLong("timestamp", System.currentTimeMillis())
                    val role = try {
                        MessageRole.valueOf(roleStr)
                    } catch (_: Exception) {
                        MessageRole.USER
                    }
                    val identityStr = mObj.optString("userIdentity", null)
                    val identity = if (!identityStr.isNullOrBlank()) {
                        try {
                            UserIdentity.valueOf(identityStr)
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                    messages.add(ConversationMessage(id = mId, role = role, text = text, timestamp = timestamp, userIdentity = identity))
                }
                list.add(ConversationSession(id = id, title = title, createdAt = createdAt, updatedAt = updatedAt, messages = messages))
            }
            // Sort most recent first
            _sessions.value = list.sortedByDescending { it.updatedAt }
        } catch (_: Exception) {
            _sessions.value = emptyList()
        }
    }

    suspend fun saveSession(session: ConversationSession) = withContext(Dispatchers.IO) {
        val current = _sessions.value.toMutableList()
        val index = current.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            current[index] = session
        } else {
            current.add(0, session)
        }
        persistList(current)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        val current = _sessions.value.filterNot { it.id == sessionId }
        persistList(current)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        persistList(emptyList())
    }

    private fun persistList(list: List<ConversationSession>) {
        val sorted = list.sortedByDescending { it.updatedAt }
        val jsonArray = JSONArray()
        for (session in sorted) {
            val obj = JSONObject().apply {
                put("id", session.id)
                put("title", session.title)
                put("createdAt", session.createdAt)
                put("updatedAt", session.updatedAt)

                val mArray = JSONArray()
                for (m in session.messages) {
                    val mObj = JSONObject().apply {
                        put("id", m.id)
                        put("role", m.role.name)
                        put("text", m.text)
                        put("timestamp", m.timestamp)
                        if (m.userIdentity != null) {
                            put("userIdentity", m.userIdentity.name)
                        }
                    }
                    mArray.put(mObj)
                }
                put("messages", mArray)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SESSIONS, jsonArray.toString()).apply()
        _sessions.value = sorted
    }
}
