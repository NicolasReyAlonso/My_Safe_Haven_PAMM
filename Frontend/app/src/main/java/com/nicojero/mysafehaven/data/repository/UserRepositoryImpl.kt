package com.nicojero.mysafehaven.data.repository
import com.nicojero.mysafehaven.data.remote.ApiService
import com.nicojero.mysafehaven.data.remote.LoginRequest
import com.nicojero.mysafehaven.domain.model.Notification
import com.nicojero.mysafehaven.domain.model.User
import com.nicojero.mysafehaven.domain.repository.UserRepository

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getNotifications(): List<Notification> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}