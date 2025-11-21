package com.example.marketpulseai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.marketpulseai.di.dashboard.DashboardScreen
import com.example.marketpulseai.ui.theme.MarketPulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarketPulseTheme {
                DashboardScreen()
            }
        }
    }
}