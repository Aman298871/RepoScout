package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.reposcout.ui.navigation.MainApp
import com.example.reposcout.ui.theme.RepoScoutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkModeManual by remember { mutableStateOf<Boolean?>(null) }
            val useDarkTheme = isDarkModeManual ?: isSystemInDarkTheme()

            RepoScoutTheme(darkTheme = useDarkTheme) {
                MainApp(
                    isDarkMode = useDarkTheme,
                    onToggleTheme = {
                        isDarkModeManual = !useDarkTheme
                    }
                )
            }
        }
    }
}
