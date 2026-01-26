package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.Country
import com.example.studylinx.repo.CountryRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CountryUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val countries: List<Country> = emptyList()
)

class CountryViewModel(
    private val repo: CountryRepo
) : ViewModel() {

    private val _ui = MutableStateFlow(CountryUiState())
    val ui: StateFlow<CountryUiState> = _ui

    init {
        viewModelScope.launch {
            repo.observeCountries().collect { list ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = null,
                    countries = list.sortedBy { it.name.lowercase() }
                )
            }
        }
    }

    companion object {
        fun factory(repo: CountryRepo) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CountryViewModel(repo) as T
            }
        }
    }
}