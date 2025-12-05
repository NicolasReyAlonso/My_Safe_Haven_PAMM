package com.nicojero.mysafehaven.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nicojero.mysafehaven.presentation.viewmodel.AuthUiState
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // Variables para mostrar errores de validación
    var validationError by remember { mutableStateOf<String?>(null) }

    // Navegar cuando el registro sea exitoso
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onNavigateToHome()
            viewModel.resetAuthState()
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
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                validationError = null
            },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthUiState.Loading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                validationError = null
            },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthUiState.Loading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                validationError = null
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            enabled = authState !is AuthUiState.Loading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                validationError = null
            },
            label = { Text("Repite la contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            enabled = authState !is AuthUiState.Loading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar error de validación
        validationError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Mostrar error del servidor
        if (authState is AuthUiState.Error) {
            Text(
                text = (authState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                // Validaciones
                when {
                    username.isBlank() -> {
                        validationError = "El nombre de usuario es requerido"
                    }
                    email.isBlank() -> {
                        validationError = "El email es requerido"
                    }
                    !email.contains("@") -> {
                        validationError = "Email inválido"
                    }
                    password.isBlank() -> {
                        validationError = "La contraseña es requerida"
                    }
                    password.length < 6 -> {
                        validationError = "La contraseña debe tener al menos 6 caracteres"
                    }
                    password != confirmPassword -> {
                        validationError = "Las contraseñas no coinciden"
                    }
                    else -> {
                        viewModel.register(username, email, password)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthUiState.Loading
        ) {
            if (authState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Ya tienes cuenta? ")
            Text(
                text = "Inicia sesión",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onNavigateToLogin()
                }
            )
        }
    }
}