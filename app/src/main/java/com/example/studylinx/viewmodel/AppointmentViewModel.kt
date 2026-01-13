package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.Appointment
import com.example.studylinx.repo.AppointmentRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AppointmentFilter { ALL, UPCOMING, PAST }

data class AppointmentsUiState(
    val userId: String = "",
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val searchQuery: String = "",
    val filter: AppointmentFilter = AppointmentFilter.ALL,
    val monthAppointments: List<Appointment> = emptyList(),
    val loading: Boolean = true
)

class AppointmentViewModel(
    private val repo: AppointmentRepo,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val _ui = MutableStateFlow(AppointmentsUiState())
    val ui: StateFlow<AppointmentsUiState> = _ui

    private var streamJob: Job? = null

    fun setUser(userId: String) {
        _ui.value = _ui.value.copy(userId = userId)
        streamMonth()
    }

    fun prevMonth() {
        _ui.value = _ui.value.copy(month = _ui.value.month.minusMonths(1))
        streamMonth()
    }

    fun nextMonth() {
        _ui.value = _ui.value.copy(month = _ui.value.month.plusMonths(1))
        streamMonth()
    }

    fun setSelectedDate(date: LocalDate) {
        _ui.value = _ui.value.copy(selectedDate = date)
    }

    fun setSearchQuery(q: String) {
        _ui.value = _ui.value.copy(searchQuery = q)
    }

    fun setFilter(filter: AppointmentFilter) {
        _ui.value = _ui.value.copy(filter = filter)
    }

    private fun streamMonth() {
        val userId = _ui.value.userId
        if (userId.isBlank()) return

        streamJob?.cancel()
        _ui.value = _ui.value.copy(loading = true)

        val month = _ui.value.month
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        streamJob = viewModelScope.launch {
            repo.streamAppointmentsInRange(userId, start, end).collect { list ->
                _ui.value = _ui.value.copy(monthAppointments = list, loading = false)
            }
        }
    }

    fun daysWithAppointmentsInMonth(): Set<LocalDate> {
        return _ui.value.monthAppointments.map {
            Instant.ofEpochMilli(it.startMillis).atZone(zoneId).toLocalDate()
        }.toSet()
    }

    fun appointmentsForSelectedDay(): List<Appointment> {
        val state = _ui.value
        val dayStart = state.selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd = state.selectedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        return state.monthAppointments
            .filter { it.startMillis in dayStart until dayEnd }
            .filter {
                when (state.filter) {
                    AppointmentFilter.ALL -> true
                    AppointmentFilter.UPCOMING -> it.startMillis >= now
                    AppointmentFilter.PAST -> it.endMillis < now
                }
            }
            .filter {
                if (state.searchQuery.isBlank()) true
                else {
                    val q = state.searchQuery.trim().lowercase()
                    it.title.lowercase().contains(q) || it.note.lowercase().contains(q)
                }
            }
    }

    fun addAppointment(title: String, note: String, startMillis: Long, endMillis: Long) {
        val userId = _ui.value.userId
        if (userId.isBlank()) return

        viewModelScope.launch {
            repo.addAppointment(
                userId,
                Appointment(
                    title = title.trim(),
                    note = note.trim(),
                    startMillis = startMillis,
                    endMillis = endMillis
                )
            )
        }
    }
}