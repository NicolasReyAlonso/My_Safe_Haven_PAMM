package com.nicojero.mysafehaven.data.remote

import com.nicojero.mysafehaven.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== AUTH ENDPOINTS ==========
    @Multipart
    @POST("register")
    suspend fun registerWithImage(
        @Part("username") username: RequestBody,
        @Part("mail") mail: RequestBody,
        @Part("password") password: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

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

    // ========== NEARBY HAVENS & SUBSCRIPTIONS ==========
    @POST("havens/nearby")
    suspend fun getNearbyHavens(@Body request: NearbyHavensRequest): Response<NearbyHavensResponse>

    @POST("havens/{haven_id}/subscribe")
    suspend fun subscribeToHaven(@Path("haven_id") havenId: Int): Response<SubscriptionResponse>

    @DELETE("havens/{haven_id}/unsubscribe")
    suspend fun unsubscribeFromHaven(@Path("haven_id") havenId: Int): Response<SubscriptionResponse>

    @GET("havens/subscribed")
    suspend fun getSubscribedHavens(): Response<List<NearbyHavenDto>>

    // ========== POST ENDPOINTS ==========
    @POST("havens/{haven_id}/posts")
    suspend fun createPost(
        @Path("haven_id") havenId: Int,
        @Body request: CreatePostRequest
    ): Response<CreatePostResponse>

    @GET("havens/{haven_id}/posts")
    suspend fun getPosts(@Path("haven_id") havenId: Int): Response<List<PostDto>>

    // ========== CHAT ENDPOINTS ==========
    @GET("havens/{havenId}/messages")
    suspend fun getMessages(
        @Path("havenId") havenId: Int
    ): Response<List<ChatMessageDto>>

    @POST("havens/{havenId}/messages")
    suspend fun sendMessage(
        @Path("havenId") havenId: Int,
        @Body request: CreateMessageRequest
    ): Response<CreateMessageResponse>
}