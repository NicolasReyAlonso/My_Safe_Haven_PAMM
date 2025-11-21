package com.nicojero.mysafehaven.domain.model

data class Notification(
    val id: String,
    val message: String,
    val isRead: Boolean
)