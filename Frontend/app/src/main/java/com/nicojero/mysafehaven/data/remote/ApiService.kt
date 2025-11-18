package com.nicojero.mysafehaven.data.remote

import com.nicojero.mysafehaven.domain.model.Notification
import com.nicojero.mysafehaven.domain.model.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("user/current")
    suspend fun getCurrentUser(): Response<User>

    @GET("notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @POST("auth/login")
    suspend fun login(
        @Body credentials: LoginRequest
    ): Response<User>
}

data class LoginRequest(
    val email: String,
    val password: String
)


