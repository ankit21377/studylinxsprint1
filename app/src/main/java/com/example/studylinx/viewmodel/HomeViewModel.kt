package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.*
import com.example.studylinx.repo.HomeRepo
import com.example.studylinx.repo.HomeRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    // ✅ overall (what your UI already uses)
    val loading: Boolean = true,
    val error: String? = null,

    // ✅ per-section (useful + fixes unit tests if they expect these names)
    val loadingCountries: Boolean = true,
    val loadingUniversities: Boolean = true,
    val loadingAppointment: Boolean = true,
    val loadingProgress: Boolean = true,

    val errorCountries: String? = null,
    val errorUniversities: String? = null,
    val errorAppointment: String? = null,
    val errorProgress: String? = null,

    val countries: List<Country> = emptyList(),
    val universities: List<University> = emptyList(),

    // ✅ same Appointment model as AppointmentActivity
    val upcomingAppointment: Appointment? = null,

    val progress: ApplicationProgress = ApplicationProgress()
)

class HomeViewModel(
    private val repo: HomeRepo = HomeRepoImpl(
        db = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance()
    )
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    init {
        // optional: seed demo data
        viewModelScope.launch {
            runCatching { repo.seedIfEmpty() }
        }

        observeCountries()
        observeUniversities()
        observeUpcomingAppointment()
        observeProgress()
    }

    private fun recomputeOverall(state: HomeUiState): HomeUiState {
        val anyLoading =
            state.loadingCountries || state.loadingUniversities || state.loadingAppointment || state.loadingProgress

        val firstError =
            state.errorCountries ?: state.errorUniversities ?: state.errorAppointment ?: state.errorProgress

        return state.copy(
            loading = anyLoading,
            error = firstError
        )
    }

    private fun observeCountries() {
        viewModelScope.launch {
            repo.observeCountries()
                .catch { e ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                loadingCountries = false,
                                errorCountries = e.message ?: "Countries error"
                            )
                        )
                    }
                }
                .collect { list ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                countries = list,
                                loadingCountries = false,
                                errorCountries = null
                            )
                        )
                    }
                }
        }
    }

    private fun observeUniversities() {
        viewModelScope.launch {
            repo.observeUniversities(limit = 10)
                .catch { e ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                loadingUniversities = false,
                                errorUniversities = e.message ?: "Universities error"
                            )
                        )
                    }
                }
                .collect { list ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                universities = list,
                                loadingUniversities = false,
                                errorUniversities = null
                            )
                        )
                    }
                }
        }
    }

    private fun observeUpcomingAppointment() {
        viewModelScope.launch {
            repo.observeUpcomingAppointment()
                .catch { e ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                loadingAppointment = false,
                                errorAppointment = e.message ?: "Appointment error"
                            )
                        )
                    }
                }
                .collect { ap ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                upcomingAppointment = ap,
                                loadingAppointment = false,
                                errorAppointment = null
                            )
                        )
                    }
                }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            repo.observeProgress()
                .catch { e ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                loadingProgress = false,
                                errorProgress = e.message ?: "Progress error"
                            )
                        )
                    }
                }
                .collect { p ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(
                                progress = p,
                                loadingProgress = false,
                                errorProgress = null
                            )
                        )
                    }
                }
        }
    }

    fun updateProgress(stepIndex: Int, completed: Boolean) {
        viewModelScope.launch {
            runCatching { repo.updateProgress(stepIndex, completed) }
                .onFailure { e ->
                    _ui.update { s ->
                        recomputeOverall(
                            s.copy(errorProgress = e.message ?: "Failed to update progress")
                        )
                    }
                }
        }
    }
}
