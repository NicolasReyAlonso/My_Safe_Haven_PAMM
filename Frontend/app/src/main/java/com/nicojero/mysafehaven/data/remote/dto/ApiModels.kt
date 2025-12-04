package com.nicojero.mysafehaven.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request DTOs
data class RegisterRequest(
    val username: String,
    val mail: String,
    val password: String,
    @SerializedName("profile_image_path")
    val profileImagePath: String? = null
)

data class LoginRequest(
    val username: String? = null,
    val mail: String? = null,
    val password: String
)

// Response DTOs
data class AuthResponse(
    val message: String,
    @SerializedName("access_token")
    val accessToken: String,
    val user: UserDto
)

data class UserDto(
    val id: Int,
    val username: String,
    val mail: String,
    @SerializedName("profile_image_path")
    val profileImagePath: String?,
    val pro: Boolean? = false
)


data class UpdateUserRequest(
    val username: String? = null,
    val mail: String? = null,
    @SerializedName("profile_image_path")
    val profileImagePath: String? = null,
    val password: String? = null,
    val pro: Boolean? = null
)

// ========== HAVEN DTOs ==========
data class CreateHavenRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double
)

data class UpdateHavenRequest(
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Double? = null
)

data class HavenDto(
    @SerializedName("haven_id")
    val havenId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double
)

data class CreateHavenResponse(
    val message: String,
    val haven: HavenDto,
    @SerializedName("remaining_havens")
    val remainingHavens: Any? = null // Puede ser Int o "ilimitado"
)

data class CanCreateHavenResponse(
    @SerializedName("can_create")
    val canCreate: Boolean,
    @SerializedName("is_pro")
    val isPro: Boolean,
    @SerializedName("current_havens")
    val currentHavens: Int,
    @SerializedName("max_havens")
    val maxHavens: Any, // Puede ser Int o "ilimitado"
    @SerializedName("remaining_havens")
    val remainingHavens: Any // Puede ser Int o "ilimitado"
)

// ========== POST DTOs ==========
data class CreatePostRequest(
    val content: String
)

data class PostDto(
    @SerializedName("post_id")
    val postId: Int,
    @SerializedName("haven_id")
    val havenId: Int,
    val content: String,
    val date: String
)

data class CreatePostResponse(
    val message: String,
    val post: PostDto
)

// ========== CHAT DTOs ==========
data class CreateMessageRequest(
    val content: String
)

data class ChatMessageDto(
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("haven_id")
    val havenId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val content: String,
    val date: String,
    val username: String
)

data class CreateMessageResponse(
    val message: String,
    @SerializedName("chat_message")
    val chatMessage: ChatMessageDto
)

// ========== ERROR RESPONSE ==========
data class ErrorResponse(
    val error: String? = null,
    val message: String? = null
)
