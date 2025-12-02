package com.nicojero.mysafehaven.domain.model

data class User(
    val id: Int,
    val username: String,
    val mail: String,
    val profileImagePath: String?,
    val pro: Boolean,
    val token: String   // <--- necesario
)
