package com.nicojero.mysafehaven.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.SessionState

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val sessionState by authViewModel.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.LoggedIn -> {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is SessionState.LoggedOut -> {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is SessionState.Checking -> {
                // Mostrar splash mientras verifica
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}