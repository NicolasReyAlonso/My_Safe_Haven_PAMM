package com.nicojero.mysafehaven.domain.repository

import com.nicojero.mysafehaven.domain.model.Notification
import com.nicojero.mysafehaven.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun getNotifications(): List<Notification>
    suspend fun login(email: String, password: String): Result<User>
}
