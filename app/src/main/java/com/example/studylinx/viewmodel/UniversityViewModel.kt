
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studylinx.repo.UniversityRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UniversityViewModel(
    private val repo: UniversityRepo
) : ViewModel() {

    private val _state = MutableStateFlow(UniversityUiState())
    val state: StateFlow<UniversityUiState> = _state

    init {
        viewModelScope.launch {
            repo.observeUniversities().collect { list ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = null,
                    all = list
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

    companion object {
        fun factory(repo: UniversityRepo) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UniversityViewModel(repo) as T
            }
        }
    }
}
