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

// Error Response
data class ErrorResponse(
    val error: String,
    val message: String? = null
)