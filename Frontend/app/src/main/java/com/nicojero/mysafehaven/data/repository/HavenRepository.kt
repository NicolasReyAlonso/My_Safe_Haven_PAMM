package com.nicojero.mysafehaven.data.repository

import com.nicojero.mysafehaven.data.remote.ApiService
import com.nicojero.mysafehaven.data.remote.dto.*
import com.nicojero.mysafehaven.domain.model.Haven
import com.nicojero.mysafehaven.domain.model.HavenLimits
import com.nicojero.mysafehaven.domain.model.Post
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class HavenResult<out T> {
    data class Success<T>(val data: T) : HavenResult<T>()
    data class Error(val message: String, val code: Int? = null) : HavenResult<Nothing>()
}


class HavenRepository @Inject constructor(
    private val apiService: ApiService
) {

    // ========== HAVEN OPERATIONS ==========

    suspend fun canCreateHaven(): HavenResult<HavenLimits> {
        return try {
            val response = apiService.canCreateHaven()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                HavenResult.Success(
                    HavenLimits(
                        canCreate = body.canCreate,
                        isPro = body.isPro,
                        currentHavens = body.currentHavens,
                        maxHavens = body.maxHavens.toString(),
                        remainingHavens = body.remainingHavens.toString()
                    )
                )
            } else {
                HavenResult.Error("Error al verificar límites", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun createHaven(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double
    ): HavenResult<Haven> {
        return try {
            val request = CreateHavenRequest(name, latitude, longitude, radius)
            val response = apiService.createHaven(request)

            if (response.isSuccessful && response.body() != null) {
                val havenDto = response.body()!!.haven
                HavenResult.Success(havenDto.toDomainModel())
            } else {
                val errorMsg = when (response.code()) {
                    403 -> "Has alcanzado el límite de havens gratuitos"
                    400 -> "Datos inválidos"
                    else -> "Error al crear haven"
                }
                HavenResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getHavens(): HavenResult<List<Haven>> {
        return try {
            val response = apiService.getHavens()
            if (response.isSuccessful && response.body() != null) {
                val havens = response.body()!!.map { it.toDomainModel() }
                HavenResult.Success(havens)
            } else {
                HavenResult.Error("Error al obtener havens", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getHavenById(havenId: Int): HavenResult<Haven> {
        return try {
            val response = apiService.getHavenById(havenId)
            if (response.isSuccessful && response.body() != null) {
                HavenResult.Success(response.body()!!.toDomainModel())
            } else {
                HavenResult.Error("Haven no encontrado", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun updateHaven(
        havenId: Int,
        name: String?,
        latitude: Double?,
        longitude: Double?,
        radius: Double?
    ): HavenResult<Haven> {
        return try {
            val request = UpdateHavenRequest(name, latitude, longitude, radius)
            val response = apiService.updateHaven(havenId, request)

            if (response.isSuccessful && response.body() != null) {
                HavenResult.Success(response.body()!!.toDomainModel())
            } else {
                HavenResult.Error("Error al actualizar haven", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun deleteHaven(havenId: Int): HavenResult<Unit> {
        return try {
            val response = apiService.deleteHaven(havenId)
            if (response.isSuccessful) {
                HavenResult.Success(Unit)
            } else {
                HavenResult.Error("Error al eliminar haven", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    // ========== NEARBY HAVENS & SUBSCRIPTIONS ==========

    suspend fun getNearbyHavens(latitude: Double, longitude: Double): HavenResult<List<Haven>> {
        return try {
            val request = NearbyHavensRequest(latitude, longitude)
            val response = apiService.getNearbyHavens(request)

            if (response.isSuccessful && response.body() != null) {
                val havens = response.body()!!.havens.map { it.toNearbyDomainModel() }
                HavenResult.Success(havens)
            } else {
                HavenResult.Error("Error al obtener havens cercanos", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun subscribeToHaven(havenId: Int): HavenResult<String> {
        return try {
            val response = apiService.subscribeToHaven(havenId)

            if (response.isSuccessful && response.body() != null) {
                HavenResult.Success(response.body()!!.message)
            } else {
                HavenResult.Error("Error al suscribirse", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun unsubscribeFromHaven(havenId: Int): HavenResult<String> {
        return try {
            val response = apiService.unsubscribeFromHaven(havenId)

            if (response.isSuccessful && response.body() != null) {
                HavenResult.Success(response.body()!!.message)
            } else {
                HavenResult.Error("Error al desuscribirse", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    // ========== POST OPERATIONS ==========

    suspend fun createPost(havenId: Int, content: String): HavenResult<Post> {
        return try {
            val request = CreatePostRequest(content)
            val response = apiService.createPost(havenId, request)

            if (response.isSuccessful && response.body() != null) {
                val postDto = response.body()!!.post
                HavenResult.Success(postDto.toDomainModel())
            } else {
                HavenResult.Error("Error al crear post", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getPosts(havenId: Int): HavenResult<List<Post>> {
        return try {
            val response = apiService.getPosts(havenId)
            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!.map { it.toDomainModel() }
                HavenResult.Success(posts)
            } else {
                HavenResult.Error("Error al obtener posts", response.code())
            }
        } catch (e: Exception) {
            HavenResult.Error(e.message ?: "Error de conexión")
        }
    }

    // ========== MAPPERS ==========

    private fun HavenDto.toDomainModel() = Haven(
        id = havenId,
        userId = userId,
        name = name,
        latitude = latitude,
        longitude = longitude,
        radius = radius
    )

    private fun NearbyHavenDto.toNearbyDomainModel() = Haven(
        id = havenId,
        userId = userId,
        name = name,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        distanceMeters = distanceMeters,
        isSubscribed = isSubscribed,
        ownerUsername = ownerUsername
    )

    private fun PostDto.toDomainModel() = Post(
        id = postId,
        havenId = havenId,
        content = content,
        date = parseDate(date)
    )

    private fun parseDate(dateStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}