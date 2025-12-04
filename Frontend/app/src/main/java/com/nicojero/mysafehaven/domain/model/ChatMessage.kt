package com.nicojero.mysafehaven.domain.model
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ChatMessage(
    val id: Int,
    val havenId: Int,
    val userId: Int,
    val username: String,
    val content: String,
    val date: LocalDateTime
) {
    fun getFormattedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return date.format(formatter)
    }
}