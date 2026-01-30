package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.repo.UniversityRepo
import com.example.studylinx.repo.UniversityRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UniversityViewModel(
    private val repo: UniversityRepo = UniversityRepoImpl()
) : ViewModel() {

    private val _state = MutableStateFlow(UniversityUiState())
    val state: StateFlow<UniversityUiState> = _state

    init {
        viewModelScope.launch {
            runCatching {
                repo.observeUniversities().collect { list ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = null,
                        all = list
                    )
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load universities"
                )
            }
        }
    }

    fun onQueryChange(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun setCountryFilter(countryId: String) {
        _state.value = _state.value.copy(countryId = countryId)
    }
}
