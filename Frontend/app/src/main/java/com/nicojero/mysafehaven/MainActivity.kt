package com.nicojero.mysafehaven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nicojero.mysafehaven.presentation.ui.MainScaffold
import com.nicojero.mysafehaven.presentation.ui.theme.MySafeHavenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySafeHavenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainScaffold()
                }
            }
        }
    }
}