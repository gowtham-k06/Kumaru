package com.kumaru.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kumaru.assistant.presentation.navigation.KumaruNavHost
import com.kumaru.assistant.presentation.theme.BackgroundWarmBase
import com.kumaru.assistant.presentation.theme.KumaruTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KumaruTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundWarmBase
                ) {
                    KumaruNavHost()
                }
            }
        }
    }
}
