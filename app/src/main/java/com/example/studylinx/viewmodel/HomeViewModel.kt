// File: com/example/studylinx/viewmodel/HomeViewModel.kt
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loadingCountries: Boolean = true,
    val loadingUniversities: Boolean = true,

    val countries: List<Country> = emptyList(),
    val universities: List<University> = emptyList(),

    val errorCountries: String? = null,
    val errorUniversities: String? = null,

    // Appointment summary (simple for home)
    val upcomingAppointmentMillis: Long = 0L,
    val upcomingAppointmentCounselor: String = "",
    val upcomingAppointmentStatus: String = "Pending"
)

class HomeViewModel(
    private val countryRepo: CountryRepo = CountryRepoImpl(),
    private val universityRepo: UniversityRepo = UniversityRepoImpl()
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    init {
        observeCountries()
        observeUniversities()

        // If you later want real upcoming appointment from Firestore/RTDB,
        // you can plug it in here. For now it keeps UI working.
        // You can also set a demo appointment like:
        // _ui.update { it.copy(upcomingAppointmentMillis = System.currentTimeMillis() + 86400000) }
    }

    private fun observeCountries() {
        viewModelScope.launch {
            countryRepo.observeCountries().collect { list ->
                _ui.update { it.copy(countries = list, loadingCountries = false, errorCountries = null) }
            }
        }
    }

    private fun observeUniversities() {
        viewModelScope.launch {
            universityRepo.observeUniversities().collect { list ->
                _ui.update { it.copy(universities = list, loadingUniversities = false, errorUniversities = null) }
            }
        }
    }
}