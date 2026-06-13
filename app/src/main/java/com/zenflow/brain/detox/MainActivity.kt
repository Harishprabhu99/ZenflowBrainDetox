package com.zenflow.brain.detox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zenflow.brain.detox.ui.navigation.BrainDetoxNavHost
import com.zenflow.brain.detox.ui.theme.ZenflowBrainDetoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenflowBrainDetoxTheme {
                BrainDetoxNavHost()
            }
        }
    }
}
