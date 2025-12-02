package com.nicojero.mysafehaven.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicojero.mysafehaven.data.repository.AuthRepository
import com.nicojero.mysafehaven.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val username: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class SessionState {
    object Checking : SessionState()
    object LoggedIn : SessionState()
    object LoggedOut : SessionState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Checking)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        checkSession()
    }

    // Verificar si hay sesión activa al iniciar la app
    private fun checkSession() {
        viewModelScope.launch {
            _sessionState.value = SessionState.Checking

            val hasSession = authRepository.hasActiveSession()

            // Opcional: verificar que el token sea válido con la API
            val isValid = if (hasSession) authRepository.verifyToken() else false

            _sessionState.value = if (hasSession && isValid) {
                SessionState.LoggedIn
            } else {
                if (hasSession && !isValid) {
                    // Token expirado, limpiar datos
                    authRepository.logout()
                }
                SessionState.LoggedOut
            }
        }
    }

    // Registrar usuario
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        // Validaciones
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Todos los campos son obligatorios")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Email inválido")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.register(username, email, password)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Success(result.username)
                    _sessionState.value = SessionState.LoggedIn
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    // Iniciar sesión
    fun login(emailOrUsername: String, password: String) {
        // Validaciones
        if (emailOrUsername.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.login(emailOrUsername, password)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Success(result.username)
                    _sessionState.value = SessionState.LoggedIn
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    // Cerrar sesión
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
            _sessionState.value = SessionState.LoggedOut
        }
    }

    // Resetear estado de autenticación (para limpiar mensajes de error)
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}