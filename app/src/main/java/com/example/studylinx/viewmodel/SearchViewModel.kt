package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.Country
import com.example.studylinx.model.University
import com.example.studylinx.repo.CountryRepo
import com.example.studylinx.repo.CountryRepoImpl
import com.example.studylinx.repo.UniversityRepo
import com.example.studylinx.repo.UniversityRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val loading: Boolean = true,
    val query: String = "",
    val countries: List<Country> = emptyList(),
    val filteredCountries: List<Country> = emptyList(),
    val universities: List<University> = emptyList(),
    val filteredUniversities: List<University> = emptyList(),
    val error: String? = null
)

class SearchViewModel(
    private val countryRepo: CountryRepo = CountryRepoImpl(),
    private val universityRepo: UniversityRepo = UniversityRepoImpl()
) : ViewModel() {

    private val _ui = MutableStateFlow(SearchUiState())
    val ui: StateFlow<SearchUiState> = _ui

    init {
        // observe countries
        viewModelScope.launch {
            try {
                countryRepo.observeCountries().collect { list ->
                    val sorted = list.sortedBy { it.name.lowercase() }
                    _ui.value = _ui.value.copy(
                        loading = false,
                        countries = sorted,
                        filteredCountries = filterCountries(sorted, _ui.value.query)
                    )
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message)
            }
        }

        // observe universities (for matching preview)
        viewModelScope.launch {
            try {
                universityRepo.observeUniversities().collect { list ->
                    _ui.value = _ui.value.copy(
                        universities = list,
                        filteredUniversities = filterUniversities(list, _ui.value.query)
                    )
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message)
            }
        }
    }

    fun setQuery(q: String) {
        _ui.value = _ui.value.copy(query = q)
        _ui.value = _ui.value.copy(
            filteredCountries = filterCountries(_ui.value.countries, q),
            filteredUniversities = filterUniversities(_ui.value.universities, q)
        )
    }

    private fun filterCountries(list: List<Country>, qRaw: String): List<Country> {
        val q = qRaw.trim().lowercase()
        if (q.isBlank()) return list
        return list.filter { it.name.lowercase().contains(q) }
    }

    private fun filterUniversities(list: List<University>, qRaw: String): List<University> {
        val q = qRaw.trim().lowercase()
        if (q.length < 2) return emptyList()
        return list.filter { u ->
            u.name.lowercase().contains(q) ||
                    u.city.lowercase().contains(q) ||
                    u.country.lowercase().contains(q) ||
                    u.courses.any { it.lowercase().contains(q) }
        }.take(10)
    }
}