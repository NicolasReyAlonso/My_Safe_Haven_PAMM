package com.nicojero.mysafehaven.data.remote

import com.nicojero.mysafehaven.data.remote.dto.AuthResponse
import com.nicojero.mysafehaven.data.remote.dto.LoginRequest
import com.nicojero.mysafehaven.data.remote.dto.RegisterRequest
import com.nicojero.mysafehaven.data.remote.dto.UserDto
import com.nicojero.mysafehaven.domain.model.Notification
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserDto>

    @GET("notifications")
    suspend fun getNotifications(): Response<List<Notification>>
}
data class LoginRequest(
    val email: String,
    val password: String
)


// Retrofit Client
object RetrofitClient {
    private const val BASE_URL = "http://10.195.126.86:5050/" // Cambia esto por tu URL

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}