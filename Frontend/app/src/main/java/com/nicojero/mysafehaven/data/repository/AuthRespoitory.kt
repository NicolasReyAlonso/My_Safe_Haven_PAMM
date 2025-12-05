package com.nicojero.mysafehaven.data.repository

import com.nicojero.mysafehaven.data.local.AuthDataStore
import com.nicojero.mysafehaven.data.remote.ApiService
import com.nicojero.mysafehaven.data.remote.dto.LoginRequest
import com.nicojero.mysafehaven.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class AuthResult {
    data class Success(
        val token: String,
        val userId: String,
        val username: String,
        val email: String
    ) : AuthResult()

    data class Error(val message: String) : AuthResult()
}

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val authDataStore: AuthDataStore
) {

    // Verificar si hay sesión activa
    suspend fun hasActiveSession(): Boolean {
        return authDataStore.token.first() != null
    }

    // Obtener token actual
    suspend fun getToken(): String? {
        return authDataStore.token.first()
    }

    // Registrar usuario
    suspend fun register(
        username: String,
        email: String,
        password: String
    ): AuthResult {
        return try {
            val request = RegisterRequest(
                username = username,
                mail = email,
                password = password
            )

            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar datos en DataStore
                authDataStore.saveAuthData(
                    token = authResponse.accessToken,
                    userId = authResponse.user.id.toString(),
                    username = authResponse.user.username,
                    email = authResponse.user.mail
                )

                AuthResult.Success(
                    token = authResponse.accessToken,
                    userId = authResponse.user.id.toString(),
                    username = authResponse.user.username,
                    email = authResponse.user.mail
                )
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "El usuario o email ya existe"
                    400 -> "Datos inválidos"
                    else -> "Error al registrar: ${response.code()}"
                }
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Error de conexión: ${e.message}")
        }
    }

    // Iniciar sesión
    suspend fun login(
        emailOrUsername: String,
        password: String
    ): AuthResult {
        return try {
            // Detectar si es email o username
            val isEmail = emailOrUsername.contains("@")

            val request = if (isEmail) {
                LoginRequest(mail = emailOrUsername, password = password)
            } else {
                LoginRequest(username = emailOrUsername, password = password)
            }

            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar datos en DataStore
                authDataStore.saveAuthData(
                    token = authResponse.accessToken,
                    userId = authResponse.user.id.toString(),
                    username = authResponse.user.username,
                    email = authResponse.user.mail
                )

                AuthResult.Success(
                    token = authResponse.accessToken,
                    userId = authResponse.user.id.toString(),
                    username = authResponse.user.username,
                    email = authResponse.user.mail
                )
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Credenciales incorrectas"
                    404 -> "Usuario no encontrado"
                    else -> "Error al iniciar sesión: ${response.code()}"
                }
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Error de conexión: ${e.message}")
        }
    }

    // Cerrar sesión
    suspend fun logout() {
        authDataStore.clearAuthData()
    }

    // Verificar token válido (opcional pero recomendado)
    suspend fun verifyToken(): Boolean {
        return try {
            val token = authDataStore.token.first() ?: return false
            val response = apiService.getCurrentUser()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}