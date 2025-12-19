package com.nicojero.mysafehaven.domain.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Post(
    val id: Int,
    val havenId: Int,
    val content: String,
    val imagePath: String? = null,  // ✅ NUEVO CAMPO
    val date: LocalDateTime
) {
    fun getRelativeTime(): String {
        val now = LocalDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(date, now)
        val hours = ChronoUnit.HOURS.between(date, now)
        val days = ChronoUnit.DAYS.between(date, now)

        return when {
            minutes < 1 -> "Justo ahora"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours h"
            days < 7 -> "Hace $days d"
            else -> date.toString()
        }
    }
}