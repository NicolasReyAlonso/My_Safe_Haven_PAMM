package com.nicojero.mysafehaven.data.repository

import com.nicojero.mysafehaven.data.remote.ApiService
import com.nicojero.mysafehaven.data.remote.dto.UserDto
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCurrentUser(): Response<UserDto> {
        return apiService.getCurrentUser()
    }

    suspend fun getUserById(userId: Int): Response<UserDto> {
        return apiService.getUserById(userId)
    }
}