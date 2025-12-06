package com.nicojero.mysafehaven.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicojero.mysafehaven.data.location.LocationManager
import com.nicojero.mysafehaven.data.repository.HavenRepository
import com.nicojero.mysafehaven.data.repository.HavenResult
import com.nicojero.mysafehaven.domain.model.Haven
import com.nicojero.mysafehaven.domain.model.HavenLimits
import com.nicojero.mysafehaven.domain.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HavenUiState {
    object Idle : HavenUiState()
    object Loading : HavenUiState()
    data class Success<T>(val data: T) : HavenUiState()
    data class Error(val message: String) : HavenUiState()
}

@HiltViewModel
class HavenViewModel @Inject constructor(
    private val havenRepository: HavenRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    // ========== HAVENS STATE ==========
    private val _havensState = MutableStateFlow<HavenUiState>(HavenUiState.Idle)
    val havensState: StateFlow<HavenUiState> = _havensState.asStateFlow()

    private val _havens = MutableStateFlow<List<Haven>>(emptyList())
    val havens: StateFlow<List<Haven>> = _havens.asStateFlow()

    private val _selectedHaven = MutableStateFlow<Haven?>(null)
    val selectedHaven: StateFlow<Haven?> = _selectedHaven.asStateFlow()

    // ========== HAVEN LIMITS STATE ==========
    private val _havenLimits = MutableStateFlow<HavenLimits?>(null)
    val havenLimits: StateFlow<HavenLimits?> = _havenLimits.asStateFlow()

    // ========== POSTS STATE ==========
    private val _postsState = MutableStateFlow<HavenUiState>(HavenUiState.Idle)
    val postsState: StateFlow<HavenUiState> = _postsState.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // ========== CREATE HAVEN STATE ==========
    private val _createHavenState = MutableStateFlow<HavenUiState>(HavenUiState.Idle)
    val createHavenState: StateFlow<HavenUiState> = _createHavenState.asStateFlow()

    // ========== CREATE POST STATE ==========
    private val _createPostState = MutableStateFlow<HavenUiState>(HavenUiState.Idle)
    val createPostState: StateFlow<HavenUiState> = _createPostState.asStateFlow()

    // ========== HAVEN OPERATIONS ==========

    fun loadHavens() {
        viewModelScope.launch {
            _havensState.value = HavenUiState.Loading
            when (val result = havenRepository.getHavens()) {
                is HavenResult.Success -> {
                    _havens.value = result.data
                    _havensState.value = HavenUiState.Success(result.data)
                }
                is HavenResult.Error -> {
                    _havensState.value = HavenUiState.Error(result.message)
                }
            }
        }
    }

    fun selectHaven(haven: Haven) {
        _selectedHaven.value = haven
        loadPosts(haven.id)
    }

    fun checkCanCreateHaven() {
        viewModelScope.launch {
            when (val result = havenRepository.canCreateHaven()) {
                is HavenResult.Success -> {
                    _havenLimits.value = result.data
                }
                is HavenResult.Error -> {
                    _createHavenState.value = HavenUiState.Error(result.message)
                }
            }
        }
    }

    fun createHaven(name: String, latitude: Double, longitude: Double, radius: Double) {
        viewModelScope.launch {
            _createHavenState.value = HavenUiState.Loading
            when (val result = havenRepository.createHaven(name, latitude, longitude, radius)) {
                is HavenResult.Success -> {
                    _createHavenState.value = HavenUiState.Success(result.data)
                    loadHavens() // Recargar lista
                }
                is HavenResult.Error -> {
                    _createHavenState.value = HavenUiState.Error(result.message)
                }
            }
        }
    }

    fun updateHaven(
        havenId: Int,
        name: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Double? = null
    ) {
        viewModelScope.launch {
            _havensState.value = HavenUiState.Loading
            when (val result = havenRepository.updateHaven(havenId, name, latitude, longitude, radius)) {
                is HavenResult.Success -> {
                    loadHavens()
                    _havensState.value = HavenUiState.Success(result.data)
                }
                is HavenResult.Error -> {
                    _havensState.value = HavenUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteHaven(havenId: Int) {
        viewModelScope.launch {
            _havensState.value = HavenUiState.Loading
            when (val result = havenRepository.deleteHaven(havenId)) {
                is HavenResult.Success -> {
                    loadHavens()
                    _selectedHaven.value = null
                }
                is HavenResult.Error -> {
                    _havensState.value = HavenUiState.Error(result.message)
                }
            }
        }
    }

    // ========== POST OPERATIONS ==========

    fun loadPosts(havenId: Int) {
        viewModelScope.launch {
            _postsState.value = HavenUiState.Loading
            when (val result = havenRepository.getPosts(havenId)) {
                is HavenResult.Success -> {
                    _posts.value = result.data
                    _postsState.value = HavenUiState.Success(result.data)
                }
                is HavenResult.Error -> {
                    _postsState.value = HavenUiState.Error(result.message)
                }
            }
        }
    }

    fun createPost(havenId: Int, content: String) {
        viewModelScope.launch {
            _createPostState.value = HavenUiState.Loading
            when (val result = havenRepository.createPost(havenId, content)) {
                is HavenResult.Success -> {
                    _createPostState.value = HavenUiState.Success(result.data)
                    loadPosts(havenId) // Recargar posts
                }
                is HavenResult.Error -> {
                    _createPostState.value = HavenUiState.Error(result.message)
                }
            }
        }
    }

    // ========== LOCATION ==========

    suspend fun getCurrentLocation() = locationManager.getCurrentLocation()

    suspend fun getLastKnownLocation() = locationManager.getLastKnownLocation()

    // ========== RESET STATES ==========

    fun resetCreateHavenState() {
        _createHavenState.value = HavenUiState.Idle
    }

    fun resetCreatePostState() {
        _createPostState.value = HavenUiState.Idle
    }

    fun resetHavensState() {
        _havensState.value = HavenUiState.Idle
    }
}