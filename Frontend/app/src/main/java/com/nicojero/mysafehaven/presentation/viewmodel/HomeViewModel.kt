package com.nicojero.mysafehaven.presentation.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicojero.mysafehaven.data.repository.HavenRepository
import com.nicojero.mysafehaven.data.repository.HavenResult
import com.nicojero.mysafehaven.domain.model.Haven
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val havenRepository: HavenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Haven>>(emptyList())
    val uiState: StateFlow<List<Haven>> = _uiState.asStateFlow()

    // Estado para el modal de detalles
    private val _havenDetails = MutableStateFlow(
        com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState()
    )
    val havenDetails: StateFlow<com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState> =
        _havenDetails.asStateFlow()

    init {
        loadSubscribedHavens()
    }

    private fun loadSubscribedHavens() {
        viewModelScope.launch {
            when (val result = havenRepository.getSubscribedHavens()) {
                is HavenResult.Success -> _uiState.value = result.data
                else -> {}
            }
        }
    }

    fun loadHavenPosts(haven: Haven) {
        viewModelScope.launch {
            _havenDetails.value = com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState(
                haven = haven,
                isLoading = true
            )

            when (val result = havenRepository.getPosts(haven.id)) {
                is HavenResult.Success -> {
                    _havenDetails.value = com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState(
                        haven = haven,
                        posts = result.data,
                        isLoading = false
                    )
                }
                is HavenResult.Error -> {
                    _havenDetails.value = com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState(
                        haven = haven,
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearHavenDetails() {
        _havenDetails.value = com.nicojero.mysafehaven.presentation.viewmodel.HavenDetailsState()
    }
}
