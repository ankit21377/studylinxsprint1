// File: com/example/studylinx/viewmodel/HomeViewModel.kt
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.*
import com.example.studylinx.repo.HomeRepo
import com.example.studylinx.repo.HomeRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,

    val countries: List<Country> = emptyList(),
    val universities: List<University> = emptyList(),

    // ✅ SAME Appointment model as AppointmentActivity (startMillis/endMillis/title/note/status...)
    val upcomingAppointment: Appointment? = null,

    val progress: ApplicationProgress = ApplicationProgress()
)

class HomeViewModel(
    private val repo: HomeRepo = HomeRepoImpl(
        db = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance()
    )
) : ViewModel() {

    private val _ui = kotlinx.coroutines.flow.MutableStateFlow(HomeUiState())
    val ui: kotlinx.coroutines.flow.StateFlow<HomeUiState> = _ui

    init {
        // optional: seed demo data
        viewModelScope.launch {
            try {
                repo.seedIfEmpty()
            } catch (_: Exception) {
                // ignore seeding error to not block UI
            }
        }

        observeCountries()
        observeUniversities()
        observeUpcomingAppointment()
        observeProgress()
    }

    private fun observeCountries() {
        viewModelScope.launch {
            repo.observeCountries()
                .catch { e -> _ui.value = _ui.value.copy(error = e.message ?: "Countries error") }
                .collect { list ->
                    _ui.value = _ui.value.copy(
                        countries = list,
                        loading = false,
                        error = null
                    )
                }
        }
    }

    private fun observeUniversities() {
        viewModelScope.launch {
            repo.observeUniversities(limit = 10)
                .catch { e -> _ui.value = _ui.value.copy(error = e.message ?: "Universities error") }
                .collect { list ->
                    _ui.value = _ui.value.copy(
                        universities = list,
                        loading = false,
                        error = null
                    )
                }
        }
    }

    private fun observeUpcomingAppointment() {
        viewModelScope.launch {
            repo.observeUpcomingAppointment()
                .catch { e -> _ui.value = _ui.value.copy(error = e.message ?: "Appointment error") }
                .collect { ap ->
                    _ui.value = _ui.value.copy(
                        upcomingAppointment = ap,
                        loading = false
                    )
                }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            repo.observeProgress()
                .catch { e -> _ui.value = _ui.value.copy(error = e.message ?: "Progress error") }
                .collect { p ->
                    _ui.value = _ui.value.copy(
                        progress = p,
                        loading = false
                    )
                }
        }
    }

    fun updateProgress(stepIndex: Int, completed: Boolean) {
        viewModelScope.launch {
            try {
                repo.updateProgress(stepIndex, completed)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message ?: "Failed to update progress")
            }
        }
    }
}
