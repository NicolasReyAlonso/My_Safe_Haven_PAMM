package com.nicojero.mysafehaven.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthDataStore(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    // Guardar token y datos del usuario
    suspend fun saveAuthData(token: String, userId: String, username: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[EMAIL_KEY] = email
        }
    }

    // Obtener token
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // Obtener datos del usuario
    val userData: Flow<UserData?> = context.dataStore.data.map { preferences ->
        val token = preferences[TOKEN_KEY]
        val userId = preferences[USER_ID_KEY]
        val username = preferences[USERNAME_KEY]
        val email = preferences[EMAIL_KEY]

        if (token != null && userId != null && username != null && email != null) {
            UserData(token, userId, username, email)
        } else {
            null
        }
    }

    // Limpiar datos (logout)
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class UserData(
    val token: String,
    val userId: String,
    val username: String,
    val email: String
)