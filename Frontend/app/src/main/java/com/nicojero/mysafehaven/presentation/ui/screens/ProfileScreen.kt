package com.nicojero.mysafehaven.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.SessionState

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val sessionState by authViewModel.sessionState.collectAsState()

    // Si el usuario cierra sesión, navegar al login
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.LoggedOut) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Perfil de Usuario",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // TODO: Mostrar datos del usuario
        Text("Información del usuario aquí")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authViewModel.logout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar sesión")
        }
    }
}