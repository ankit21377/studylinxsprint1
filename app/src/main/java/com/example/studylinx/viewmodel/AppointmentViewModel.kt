// File: com/example/studylinx/viewmodel/AppointmentViewModel.kt
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import com.example.studylinx.model.Appointment
import com.example.studylinx.repo.AppointmentRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.*
import java.util.Locale

enum class AppointmentFilter { ALL, UPCOMING, PAST }

data class AppointmentUiState(
    val userId: String = "",
    val loading: Boolean = false,
    val error: String? = null,

    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),

    val monthAppointments: List<Appointment> = emptyList(),

    val searchQuery: String = "",
    val filter: AppointmentFilter = AppointmentFilter.ALL
)

class AppointmentViewModel(
    private val repo: AppointmentRepo
) : ViewModel() {

    private val _ui = MutableStateFlow(AppointmentUiState())
    val ui: StateFlow<AppointmentUiState> = _ui

    private var started = false

    fun setUser(userId: String) {
        if (userId.isBlank()) return
        _ui.value = _ui.value.copy(userId = userId)
        startObserveOnce()
    }

    private fun startObserveOnce() {
        if (started) return
        val uid = _ui.value.userId
        if (uid.isBlank()) return

        started = true
        _ui.value = _ui.value.copy(loading = true, error = null)

        repo.observeAppointments(
            userId = uid,
            onUpdate = { list ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    monthAppointments = list.sortedBy { it.startMillis },
                    error = null
                )
            },
            onError = { err ->
                _ui.value = _ui.value.copy(loading = false, error = err)
            }
        )
    }

    fun prevMonth() {
        _ui.value = _ui.value.copy(month = _ui.value.month.minusMonths(1))
    }

    fun nextMonth() {
        _ui.value = _ui.value.copy(month = _ui.value.month.plusMonths(1))
    }

    fun setSelectedDate(date: LocalDate) {
        _ui.value = _ui.value.copy(selectedDate = date)
    }

    fun setSearchQuery(q: String) {
        _ui.value = _ui.value.copy(searchQuery = q)
    }

    fun setFilter(f: AppointmentFilter) {
        _ui.value = _ui.value.copy(filter = f)
    }

    // ✅ FIXED addAppointment (matches model perfectly)
    fun addAppointment(title: String, note: String, startMillis: Long, endMillis: Long) {
        val uid = _ui.value.userId
        if (uid.isBlank()) {
            _ui.value = _ui.value.copy(error = "User not set")
            return
        }

        val safeTitle = title.trim()
        if (safeTitle.isBlank()) {
            _ui.value = _ui.value.copy(error = "Title is required")
            return
        }

        val safeEnd = if (endMillis <= startMillis) startMillis + 60 * 60 * 1000 else endMillis

        val ap = Appointment(
            id = "",
            userId = uid,
            title = safeTitle,
            note = note.trim(),
            startMillis = startMillis,
            endMillis = safeEnd,
            status = "Pending",
            createdAt = System.currentTimeMillis()
        )

        repo.addAppointment(uid, ap) { ok, msg ->
            if (!ok) _ui.value = _ui.value.copy(error = msg ?: "Failed to add appointment")
        }
    }

    fun appointmentsForSelectedDay(): List<Appointment> {
        val s = _ui.value
        val zone = ZoneId.systemDefault()

        val dayStart = s.selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = s.selectedDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val now = System.currentTimeMillis()
        val q = s.searchQuery.trim().lowercase(Locale.getDefault())

        return s.monthAppointments
            .asSequence()
            .filter { it.startMillis in dayStart until dayEnd }
            .filter {
                when (s.filter) {
                    AppointmentFilter.ALL -> true
                    AppointmentFilter.UPCOMING -> it.endMillis >= now
                    AppointmentFilter.PAST -> it.endMillis < now
                }
            }
            .filter {
                if (q.isBlank()) true
                else it.title.lowercase(Locale.getDefault()).contains(q) ||
                        it.note.lowercase(Locale.getDefault()).contains(q)
            }
            .sortedBy { it.startMillis }
            .toList()
    }

    fun daysWithAppointmentsInMonth(): Set<LocalDate> {
        val s = _ui.value
        val zone = ZoneId.systemDefault()

        val startOfMonth = s.month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfMonth = s.month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

        return s.monthAppointments
            .asSequence()
            .filter { it.startMillis in startOfMonth until endOfMonth }
            .map { Instant.ofEpochMilli(it.startMillis).atZone(zone).toLocalDate() }
            .toSet()
    }
}
