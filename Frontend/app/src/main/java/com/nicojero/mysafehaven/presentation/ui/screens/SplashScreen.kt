package com.nicojero.mysafehaven.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.SessionState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        // Esperar un poco para mostrar el splash
        delay(1500)

        when (sessionState) {
            is SessionState.LoggedIn -> onNavigateToHome()
            is SessionState.LoggedOut -> onNavigateToLogin()
            else -> {
                // Si aún está cargando, esperar más
                delay(1000)
                if (sessionState is SessionState.LoggedOut) {
                    onNavigateToLogin()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "My Safe Haven",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Cargando...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}