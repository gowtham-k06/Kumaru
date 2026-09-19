package com.kumaru.assistant

import android.app.Application

/**
 * Root Application class for Kumaru personal assistant.
 * Provides application-level initialization and future dependency container lifecycle.
 */
class KumaruApplication : Application() {
    companion object {
        private var _instance: KumaruApplication? = null
        val instance: KumaruApplication
            get() = _instance ?: throw IllegalStateException("KumaruApplication is not initialized")
        val appContext: android.content.Context
            get() = _instance?.applicationContext
                ?: throw IllegalStateException("KumaruApplication appContext is not initialized")
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }
}
