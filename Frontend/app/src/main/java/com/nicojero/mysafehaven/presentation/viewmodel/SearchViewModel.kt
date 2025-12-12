// presentation/viewmodel/SearchViewModel.kt
package com.nicojero.mysafehaven.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicojero.mysafehaven.data.location.LocationManager
import com.nicojero.mysafehaven.data.location.UserLocation
import com.nicojero.mysafehaven.data.repository.HavenRepository
import com.nicojero.mysafehaven.data.repository.HavenResult
import com.nicojero.mysafehaven.domain.model.Haven
import com.nicojero.mysafehaven.domain.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUiState {
    object Loading : SearchUiState()
    object LocationDisabled : SearchUiState()
    data class Success(val nearbyHavens: List<Haven>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

data class HavenDetailsState(
    val haven: Haven? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val havenRepository: HavenRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<UserLocation?>(null)
    val currentLocation: StateFlow<UserLocation?> = _currentLocation.asStateFlow()

    private val _havenDetails = MutableStateFlow(HavenDetailsState())
    val havenDetails: StateFlow<HavenDetailsState> = _havenDetails.asStateFlow()

    private val _subscriptionMessage = MutableSharedFlow<String>()
    val subscriptionMessage: SharedFlow<String> = _subscriptionMessage.asSharedFlow()

    init {
        checkLocationAndLoadHavens()
    }

    fun checkLocationAndLoadHavens() {
        viewModelScope.launch {
            if (!locationManager.hasLocationPermission()) {
                _uiState.value = SearchUiState.LocationDisabled
                return@launch
            }

            _uiState.value = SearchUiState.Loading

            val location = locationManager.getCurrentLocation()
            if (location != null) {
                _currentLocation.value = location
                loadNearbyHavens(location.latitude, location.longitude)
            } else {
                _uiState.value = SearchUiState.Error("No se pudo obtener la ubicación")
            }
        }
    }

    private fun loadNearbyHavens(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            when (val result = havenRepository.getNearbyHavens(latitude, longitude)) {
                is Result.Success -> {
                    _uiState.value = SearchUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = SearchUiState.Error(result.message)
                }
            }
        }
    }

    fun subscribeToHaven(havenId: Int) {
        viewModelScope.launch {
            when (val result = havenRepository.subscribeToHaven(havenId)) {
                is Result.Success -> {
                    _subscriptionMessage.emit(result.data)
                    // Recargar havens para actualizar el estado de suscripción
                    _currentLocation.value?.let {
                        loadNearbyHavens(it.latitude, it.longitude)
                    }
                }
                is Result.Error -> {
                    _subscriptionMessage.emit(result.message)
                }
            }
        }
    }

    fun unsubscribeFromHaven(havenId: Int) {
        viewModelScope.launch {
            when (val result = havenRepository.unsubscribeFromHaven(havenId)) {
                is Result.Success -> {
                    _subscriptionMessage.emit(result.data)
                    _currentLocation.value?.let {
                        loadNearbyHavens(it.latitude, it.longitude)
                    }
                }
                is Result.Error -> {
                    _subscriptionMessage.emit(result.message)
                }
            }
        }
    }

    fun loadHavenDetails(haven: Haven) {
        viewModelScope.launch {
            _havenDetails.value = HavenDetailsState(haven = haven, isLoading = true)

            when (val result = havenRepository.getHavenPosts(haven.id)) {
                is Result.Success -> {
                    _havenDetails.value = HavenDetailsState(
                        haven = haven,
                        posts = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _havenDetails.value = HavenDetailsState(
                        haven = haven,
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearHavenDetails() {
        _havenDetails.value = HavenDetailsState()
    }

    fun refreshLocation() {
        checkLocationAndLoadHavens()
    }
}