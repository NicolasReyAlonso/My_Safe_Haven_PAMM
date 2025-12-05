package com.nicojero.mysafehaven.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicojero.mysafehaven.data.repository.AuthRepository
import com.nicojero.mysafehaven.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SessionState {
    object Idle : SessionState()
    object Loading : SessionState()
    data class LoggedIn(
        val userId: String,
        val username: String,
        val email: String
    ) : SessionState()
    object LoggedOut : SessionState()
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel  // ✅ Agregar esta anotación
class AuthViewModel @Inject constructor(  // ✅ Agregar @Inject
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _sessionState.value = SessionState.Loading

            val hasSession = authRepository.hasActiveSession()

            if (hasSession) {
                // TODO: Obtener datos del usuario actual si es necesario
                _sessionState.value = SessionState.LoggedIn(
                    userId = "temp",
                    username = "temp",
                    email = "temp"
                )
            } else {
                _sessionState.value = SessionState.LoggedOut
            }
        }
    }

    fun login(emailOrUsername: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading

            when (val result = authRepository.login(emailOrUsername, password)) {
                is AuthResult.Success -> {
                    _authState.value = AuthUiState.Success
                    _sessionState.value = SessionState.LoggedIn(
                        userId = result.userId,
                        username = result.username,
                        email = result.email
                    )
                }
                is AuthResult.Error -> {
                    _authState.value = AuthUiState.Error(result.message)
                    _sessionState.value = SessionState.LoggedOut
                }
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading

            when (val result = authRepository.register(username, email, password)) {
                is AuthResult.Success -> {
                    _authState.value = AuthUiState.Success
                    _sessionState.value = SessionState.LoggedIn(
                        userId = result.userId,
                        username = result.username,
                        email = result.email
                    )
                }
                is AuthResult.Error -> {
                    _authState.value = AuthUiState.Error(result.message)
                    _sessionState.value = SessionState.LoggedOut
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _sessionState.value = SessionState.LoggedOut
            _authState.value = AuthUiState.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = AuthUiState.Idle
    }
}