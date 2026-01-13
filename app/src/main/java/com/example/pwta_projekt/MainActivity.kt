package com.example.pwta_projekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.pwta_projekt.ui.navigation.AppNavigation
import com.example.pwta_projekt.ui.theme.PWTA_projektTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PWTA_projektTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}