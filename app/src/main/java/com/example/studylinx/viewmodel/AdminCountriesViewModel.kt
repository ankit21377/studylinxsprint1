
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.Country
import com.example.studylinx.repo.CountryRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminCountriesState(
    val loading: Boolean = true,
    val error: String? = null,
    val saving: Boolean = false,
    val countries: List<Country> = emptyList()
)

class AdminCountriesViewModel(
    private val repo: CountryRepo
) : ViewModel() {

    private val _ui = MutableStateFlow(AdminCountriesState())
    val ui: StateFlow<AdminCountriesState> = _ui

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

    fun addNew(name: String, flagUrl: String) {
        val n = name.trim()
        if (n.isBlank()) {
            _ui.value = _ui.value.copy(error = "Country name required")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            runCatching {
                repo.addCountry(Country(name = n, flagUrl = flagUrl.trim()))
            }.onFailure {
                _ui.value = _ui.value.copy(error = it.message ?: "Failed to add")
            }
            _ui.value = _ui.value.copy(saving = false)
        }
    }

    fun update(countryId: String, name: String, flagUrl: String) {
        val n = name.trim()
        if (countryId.isBlank() || n.isBlank()) {
            _ui.value = _ui.value.copy(error = "Invalid country data")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            runCatching {
                repo.upsertCountry(Country(id = countryId, name = n, flagUrl = flagUrl.trim()))
            }.onFailure {
                _ui.value = _ui.value.copy(error = it.message ?: "Failed to update")
            }
            _ui.value = _ui.value.copy(saving = false)
        }
    }

    fun delete(countryId: String) {
        if (countryId.isBlank()) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            runCatching { repo.deleteCountry(countryId) }
                .onFailure { _ui.value = _ui.value.copy(error = it.message ?: "Failed to delete") }
            _ui.value = _ui.value.copy(saving = false)
        }
    }

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }

    companion object {
        fun factory(repo: CountryRepo) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminCountriesViewModel(repo) as T
            }
        }
    }
}
