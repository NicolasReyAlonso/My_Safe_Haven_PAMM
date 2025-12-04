package com.nicojero.mysafehaven.domain.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Haven(
    val id: Int,
    val userId: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double
)
data class HavenLimits(
    val canCreate: Boolean,
    val isPro: Boolean,
    val currentHavens: Int,
    val maxHavens: String, // "3" o "ilimitado"
    val remainingHavens: String // "2" o "ilimitado"
)