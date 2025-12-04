package com.nicojero.mysafehaven.data.remote

import com.nicojero.mysafehaven.data.remote.dto.*
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
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("users/{user_id}")
    suspend fun getUserById(@Path("user_id") userId: Int): Response<UserDto>

    @PUT("users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Body request: UpdateUserRequest
    ): Response<UserDto>

    // ========== HAVEN ENDPOINTS ==========
    @GET("havens/can-create")
    suspend fun canCreateHaven(): Response<CanCreateHavenResponse>

    @POST("havens")
    suspend fun createHaven(@Body request: CreateHavenRequest): Response<CreateHavenResponse>

    @GET("havens")
    suspend fun getHavens(): Response<List<HavenDto>>

    @GET("havens/{haven_id}")
    suspend fun getHavenById(@Path("haven_id") havenId: Int): Response<HavenDto>

    @PUT("havens/{haven_id}")
    suspend fun updateHaven(
        @Path("haven_id") havenId: Int,
        @Body request: UpdateHavenRequest
    ): Response<HavenDto>

    @DELETE("havens/{haven_id}")
    suspend fun deleteHaven(@Path("haven_id") havenId: Int): Response<Map<String, String>>

    // ========== POST ENDPOINTS ==========
    @POST("havens/{haven_id}/posts")
    suspend fun createPost(
        @Path("haven_id") havenId: Int,
        @Body request: CreatePostRequest
    ): Response<CreatePostResponse>

    @GET("havens/{haven_id}/posts")
    suspend fun getPosts(@Path("haven_id") havenId: Int): Response<List<PostDto>>

    // ========== CHAT ENDPOINTS ==========
    @POST("havens/{haven_id}/messages")
    suspend fun sendMessage(
        @Path("haven_id") havenId: Int,
        @Body request: CreateMessageRequest
    ): Response<CreateMessageResponse>

    @GET("havens/{haven_id}/messages")
    suspend fun getMessages(@Path("haven_id") havenId: Int): Response<List<ChatMessageDto>>
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
