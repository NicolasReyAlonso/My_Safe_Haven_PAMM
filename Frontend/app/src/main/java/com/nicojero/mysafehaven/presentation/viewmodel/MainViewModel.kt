package com.nicojero.mysafehaven.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicojero.mysafehaven.data.remote.RetrofitClient
import com.nicojero.mysafehaven.data.repository.UserRepositoryImpl
import com.nicojero.mysafehaven.domain.model.Notification
import com.nicojero.mysafehaven.domain.model.User
import com.nicojero.mysafehaven.domain.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl(RetrofitClient.apiService)
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set

    var notifications by mutableStateOf<List<Notification>>(emptyList())
        private set

    var hasUnreadNotifications by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            isLoading = true
            user = userRepository.getCurrentUser()
            notifications = userRepository.getNotifications()
            hasUnreadNotifications = notifications.any { !it.isRead }
            isLoading = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            val result = userRepository.login(email, password)
            result.onSuccess {
                user = it
                loadUserData()
            }
            isLoading = false
        }
    }

    fun refreshData() {
        loadUserData()
    }
}