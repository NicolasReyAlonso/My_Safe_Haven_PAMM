package com.nicojero.mysafehaven.presentation.viewmodel

import android.content.Context
import android.net.Uri
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

@HiltViewModel
class AuthViewModel @Inject constructor(
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
                // ✅ Obtener el usuario actual desde la API
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _sessionState.value = SessionState.LoggedIn(
                        userId = user.id.toString(),
                        username = user.username,
                        email = user.mail
                    )
                } else {
                    // Si falla la obtención del usuario, cerrar sesión
                    authRepository.logout()
                    _sessionState.value = SessionState.LoggedOut
                }
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
                    _sessionState.value = SessionState.LoggedIn(
                        userId = result.userId,
                        username = result.username,
                        email = result.email
                    )
                    _authState.value = AuthUiState.Success
                }
                is AuthResult.Error -> {
                    _sessionState.value = SessionState.LoggedOut
                    _authState.value = AuthUiState.Error(result.message)
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

    fun registerWithImage(
        username: String,
        email: String,
        password: String,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading

            when (val result = authRepository.registerWithImage(
                username = username,
                email = email,
                password = password,
                imageUri = imageUri,
                context = context
            )) {
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