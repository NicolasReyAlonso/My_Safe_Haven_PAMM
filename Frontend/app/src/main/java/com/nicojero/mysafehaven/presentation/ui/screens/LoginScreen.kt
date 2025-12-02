package com.nicojero.mysafehaven.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nicojero.mysafehaven.presentation.viewmodel.AuthState
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    // Navegar cuando el login sea exitoso
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
            authViewModel.resetAuthState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emailOrUsername,
            onValueChange = { emailOrUsername = it },
            label = { Text("Email o nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            enabled = authState !is AuthState.Loading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar error si existe
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                authViewModel.login(emailOrUsername, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Primera vez? ")
            Text(
                text = "Regístrate",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}