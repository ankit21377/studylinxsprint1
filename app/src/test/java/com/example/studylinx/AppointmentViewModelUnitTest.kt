package com.example.studylinx

import com.example.studylinx.model.Appointment
import com.example.studylinx.repo.AppointmentRepo
import com.example.studylinx.viewmodel.AppointmentFilter
import com.example.studylinx.viewmodel.AppointmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentViewModelUnitTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: AppointmentRepo

    // captured callbacks from observeAppointments(...)
    private lateinit var onUpdate: (List<Appointment>) -> Unit
    private lateinit var onError: (String) -> Unit

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = mock()

        // Capture callbacks from observeAppointments(userId, onUpdate, onError)
        whenever(repo.observeAppointments(any(), any(), any())).thenAnswer { inv ->
            onUpdate = inv.getArgument(1)
            onError = inv.getArgument(2)
            Unit
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setUser_startsObserve_andSetsLoadingTrue() = runTest {
        val vm = AppointmentViewModel(repo)

        vm.setUser("uid123")
        advanceUntilIdle()

        assertTrue(vm.ui.value.loading)
        verify(repo).observeAppointments(eq("uid123"), any(), any())
    }

    @Test
    fun observeUpdate_setsAppointments_andStopsLoading() = runTest {
        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")

        val now = System.currentTimeMillis()
        val list = listOf(
            Appointment(
                id = "1",
                userId = "uid123",
                title = "Meet",
                note = "Test",
                startMillis = now,
                endMillis = now + 60_000,
                status = "Pending",
                createdAt = now
            )
        )

        onUpdate(list)
        advanceUntilIdle()

        val state = vm.ui.value
        assertFalse(state.loading)
        assertEquals(1, state.monthAppointments.size)
        assertEquals("Meet", state.monthAppointments[0].title)
        assertNull(state.error)
    }

    @Test
    fun observeError_setsError_andStopsLoading() = runTest {
        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")

        onError("Boom")
        advanceUntilIdle()

        assertFalse(vm.ui.value.loading)
        assertEquals("Boom", vm.ui.value.error)
    }

    @Test
    fun prevMonth_changesMonth_only() = runTest {
        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")
        advanceUntilIdle()

        val before = vm.ui.value.month
        vm.prevMonth()
        advanceUntilIdle()

        assertEquals(before.minusMonths(1), vm.ui.value.month)

        // Your current VM observes once only
        verify(repo, times(1)).observeAppointments(eq("uid123"), any(), any())
    }

    @Test
    fun nextMonth_changesMonth_only() = runTest {
        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")
        advanceUntilIdle()

        val before = vm.ui.value.month
        vm.nextMonth()
        advanceUntilIdle()

        assertEquals(before.plusMonths(1), vm.ui.value.month)
        verify(repo, times(1)).observeAppointments(eq("uid123"), any(), any())
    }

    @Test
    fun daysWithAppointmentsInMonth_returnsUniqueDays() = runTest {
        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")

        val zone = ZoneId.systemDefault()

        val d1 = LocalDate.of(2026, 1, 10)
        val d2 = LocalDate.of(2026, 1, 12)

        fun millis(d: LocalDate, h: Int, m: Int): Long =
            d.atTime(h, m).atZone(zone).toInstant().toEpochMilli()

        val list = listOf(
            Appointment(title = "A1", note = "n", startMillis = millis(d1, 10, 0), endMillis = millis(d1, 11, 0)),
            Appointment(title = "A2", note = "n", startMillis = millis(d2, 9, 0), endMillis = millis(d2, 10, 0)),
            Appointment(title = "A1-2", note = "n", startMillis = millis(d1, 12, 0), endMillis = millis(d1, 13, 0))
        )

        onUpdate(list)
        advanceUntilIdle()

        val days = vm.daysWithAppointmentsInMonth()
        assertEquals(setOf(d1, d2), days)
    }

    @Test
    fun appointmentsForSelectedDay_filtersBySelectedDate() = runTest {
        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")

        val zone = ZoneId.systemDefault()
        val selected = LocalDate.of(2026, 1, 10)
        val other = LocalDate.of(2026, 1, 11)

        vm.setSelectedDate(selected)

        fun millis(d: LocalDate, h: Int): Long =
            d.atTime(h, 0).atZone(zone).toInstant().toEpochMilli()

        onUpdate(
            listOf(
                Appointment(title = "SelectedDay", note = "note", startMillis = millis(selected, 10), endMillis = millis(selected, 11)),
                Appointment(title = "OtherDay", note = "note", startMillis = millis(other, 10), endMillis = millis(other, 11))
            )
        )
        advanceUntilIdle()

        val result = vm.appointmentsForSelectedDay()
        assertEquals(1, result.size)
        assertEquals("SelectedDay", result[0].title)
    }

    @Test
    fun appointmentsForSelectedDay_searchQuery_filtersByTitleOrNote() = runTest {
        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")

        val zone = ZoneId.systemDefault()
        val selected = LocalDate.of(2026, 1, 10)
        vm.setSelectedDate(selected)

        fun millis(h: Int): Long =
            selected.atTime(h, 0).atZone(zone).toInstant().toEpochMilli()

        onUpdate(
            listOf(
                Appointment(title = "Doctor Visit", note = "bring reports", startMillis = millis(10), endMillis = millis(11)),
                Appointment(title = "Meeting", note = "project discussion", startMillis = millis(12), endMillis = millis(13))
            )
        )
        advanceUntilIdle()

        vm.setSearchQuery("doctor")
        val result = vm.appointmentsForSelectedDay()

        assertEquals(1, result.size)
        assertEquals("Doctor Visit", result[0].title)
    }

    @Test
    fun addAppointment_callsRepo_whenUserIsSet() = runTest {
        whenever(repo.addAppointment(any(), any(), any())).thenAnswer { inv ->
            val cb = inv.getArgument<(Boolean, String?) -> Unit>(2)
            cb(true, null)
            Unit
        }

        val vm = AppointmentViewModel(repo)
        vm.setUser("uid123")
        advanceUntilIdle()

        vm.addAppointment("  Title  ", "  Note  ", 10L, 20L)
        advanceUntilIdle()

        verify(repo).addAppointment(
            eq("uid123"),
            check {
                assertEquals("Title", it.title)
                assertEquals("Note", it.note)
                assertEquals(10L, it.startMillis)
                assertEquals(20L, it.endMillis)
            },
            any()
        )
    }

    @Test
    fun addAppointment_doesNothing_whenUserIdBlank() = runTest {
        val vm = AppointmentViewModel(repo)

        vm.addAppointment("Title", "Note", 10L, 20L)
        advanceUntilIdle()

        verify(repo, never()).addAppointment(any(), any(), any())
    }
}
