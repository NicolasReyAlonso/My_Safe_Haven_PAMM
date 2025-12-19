package com.nicojero.mysafehaven.data.repository

import android.content.Context
import android.net.Uri
import com.nicojero.mysafehaven.data.local.AuthDataStore
import com.nicojero.mysafehaven.data.remote.ApiService
import com.nicojero.mysafehaven.data.remote.dto.LoginRequest
import com.nicojero.mysafehaven.data.remote.dto.RegisterRequest
import com.nicojero.mysafehaven.data.remote.dto.UserDto
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
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

    // Registrar usuario CON imagen
    suspend fun registerWithImage(
        username: String,
        email: String,
        password: String,
        imageUri: Uri?,
        context: Context
    ): AuthResult {
        return try {
            // Preparar los campos de texto
            val usernameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
            val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val passwordBody = password.toRequestBody("text/plain".toMediaTypeOrNull())

            // Preparar la imagen (si existe)
            val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                val file = uriToFile(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profile_image", file.name, requestFile)
            }

            val response = apiService.registerWithImage(
                username = usernameBody,
                mail = emailBody,
                password = passwordBody,
                profileImage = imagePart
            )

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

    // Registrar usuario SIN imagen (mantener compatibilidad)
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

    // Función auxiliar para convertir Uri a File
    private fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val tempFile = File(context.cacheDir, "temp_profile_image_${System.currentTimeMillis()}.jpg")

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    // Iniciar sesión (sin cambios)
    suspend fun login(
        emailOrUsername: String,
        password: String
    ): AuthResult {
        return try {
            val isEmail = emailOrUsername.contains("@")

            val request = if (isEmail) {
                LoginRequest(mail = emailOrUsername, password = password)
            } else {
                LoginRequest(username = emailOrUsername, password = password)
            }

            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

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

    suspend fun logout() {
        authDataStore.clearAuthData()
    }
    suspend fun getCurrentUser(): UserDto? {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


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