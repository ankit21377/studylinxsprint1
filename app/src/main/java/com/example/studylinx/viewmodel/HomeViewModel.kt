package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.*
import com.example.studylinx.repo.HomeRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: HomeRepo
) : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries

    private val _universities = MutableStateFlow<List<University>>(emptyList())
    val universities: StateFlow<List<University>> = _universities

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

    private val _progress = MutableStateFlow(ApplicationProgress())
    val progress: StateFlow<ApplicationProgress> = _progress

    val loading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun startHome(limitUniversities: Int = 6) {
        viewModelScope.launch {
            try {
                loading.value = true
                repo.seedIfEmpty()

                launch { repo.observeCountries().collect { _countries.value = it } }
                launch { repo.observeUniversities(limitUniversities).collect { _universities.value = it } }
                launch { repo.observeUpcomingAppointment().collect { _appointment.value = it } }
                launch { repo.observeProgress().collect { _progress.value = it } }

            } catch (e: Exception) {
                error.value = e.message
            } finally {
                loading.value = false
            }
        }
    }

    fun setStepCompleted(stepIndex: Int, completed: Boolean) {
        viewModelScope.launch {
            repo.updateProgress(stepIndex, completed)
        }
    }
}