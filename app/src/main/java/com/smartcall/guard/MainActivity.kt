package com.smartcall.guard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smartcall.guard.ui.navigation.AppNavHost
import com.smartcall.guard.ui.theme.SmartCallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCallTheme {
                AppNavHost()
            }
        }
    }
}
