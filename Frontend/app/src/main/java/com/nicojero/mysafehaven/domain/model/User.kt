package com.nicojero.mysafehaven.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isLoggedIn: Boolean
)