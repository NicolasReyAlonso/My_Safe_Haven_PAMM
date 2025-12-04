package com.nicojero.mysafehaven.domain.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class Post(
    val id: Int,
    val havenId: Int,
    val content: String,
    val date: LocalDateTime
) {
    fun getFormattedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return date.format(formatter)
    }

    fun getRelativeTime(): String {
        val now = LocalDateTime.now()
        val minutes = java.time.Duration.between(date, now).toMinutes()

        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "${minutes}m"
            minutes < 1440 -> "${minutes / 60}h"
            minutes < 10080 -> "${minutes / 1440}d"
            minutes < 43200 -> "${minutes / 10080}sem"
            else -> getFormattedDate()
        }
    }
}
