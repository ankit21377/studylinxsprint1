package com.example.studylinx

import com.example.studylinx.model.Appointment

import com.example.studylinx.repo.AppointmentRepo

import com.example.studylinx.viewmodel.AppointmentFilter

import com.example.studylinx.viewmodel.AppointmentViewModel

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.flow.MutableSharedFlow

import kotlinx.coroutines.test.*

import org.junit.After

import org.junit.Assert.*

import org.junit.Before

import org.junit.Test

import org.mockito.kotlin.*

import java.time.*

@OptIn(ExperimentalCoroutinesApi::class)

class AppointmentViewModelUnitTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: AppointmentRepo

    private lateinit var flow: MutableSharedFlow<List<Appointment>>

    private val zoneId: ZoneId = ZoneId.of("UTC")

    @Before

    fun setup() {

        Dispatchers.setMain(dispatcher)

        repo = mock()

        flow = MutableSharedFlow(replay = 1)

        whenever(repo.streamAppointmentsInRange(any(), any(), any())).thenReturn(flow)

    }

    @After

    fun tearDown() {

        Dispatchers.resetMain()

    }

    @Test

    fun setUser_startsStreamingAndUpdatesLoading() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        advanceUntilIdle()

        // should start loading while waiting for first emission

        assertTrue(vm.ui.value.loading)

        verify(repo).streamAppointmentsInRange(eq("uid123"), any(), any())

    }

    @Test

    fun streamEmission_updatesMonthAppointments_andStopsLoading() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        val now = System.currentTimeMillis()

        val list = listOf(

            Appointment(title = "Meet", note = "Test", startMillis = now, endMillis = now + 60_000)

        )

        flow.emit(list)

        advanceUntilIdle()

        val state = vm.ui.value

        assertFalse(state.loading)

        assertEquals(1, state.monthAppointments.size)

        assertEquals("Meet", state.monthAppointments[0].title)

    }

    @Test

    fun prevMonth_changesMonth_andRestartsStream() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        advanceUntilIdle()

        val before = vm.ui.value.month

        vm.prevMonth()

        advanceUntilIdle()

        assertEquals(before.minusMonths(1), vm.ui.value.month)

        // called again after changing month

        verify(repo, atLeast(2)).streamAppointmentsInRange(eq("uid123"), any(), any())

    }

    @Test

    fun nextMonth_changesMonth_andRestartsStream() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        advanceUntilIdle()

        val before = vm.ui.value.month

        vm.nextMonth()

        advanceUntilIdle()

        assertEquals(before.plusMonths(1), vm.ui.value.month)

        verify(repo, atLeast(2)).streamAppointmentsInRange(eq("uid123"), any(), any())

    }

    @Test

    fun daysWithAppointmentsInMonth_returnsUniqueDays() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        val d1 = LocalDate.of(2026, 1, 10)

        val d2 = LocalDate.of(2026, 1, 12)

        val a1Start = d1.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        val a1End = d1.atTime(11, 0).atZone(zoneId).toInstant().toEpochMilli()

        val a2Start = d2.atTime(9, 0).atZone(zoneId).toInstant().toEpochMilli()

        val a2End = d2.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        val list = listOf(

            Appointment("A1", "n", a1Start, a1End),

            Appointment("A2", "n", a2Start, a2End),

            Appointment("A1-SecondSameDay", "n", a1Start + 1_000, a1End + 1_000)

        )

        flow.emit(list)

        advanceUntilIdle()

        val days = vm.daysWithAppointmentsInMonth()

        assertEquals(setOf(d1, d2), days)

    }

    @Test

    fun appointmentsForSelectedDay_filtersBySelectedDate() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        val selected = LocalDate.of(2026, 1, 10)

        vm.setSelectedDate(selected)

        val other = LocalDate.of(2026, 1, 11)

        val s1 = selected.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        val e1 = selected.atTime(11, 0).atZone(zoneId).toInstant().toEpochMilli()

        val s2 = other.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        val e2 = other.atTime(11, 0).atZone(zoneId).toInstant().toEpochMilli()

        flow.emit(

            listOf(

                Appointment("SelectedDay", "note", s1, e1),

                Appointment("OtherDay", "note", s2, e2)

            )

        )

        advanceUntilIdle()

        val result = vm.appointmentsForSelectedDay()

        assertEquals(1, result.size)

        assertEquals("SelectedDay", result[0].title)

    }

    @Test

    fun appointmentsForSelectedDay_searchQuery_filtersByTitleOrNote() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        val selected = LocalDate.of(2026, 1, 10)

        vm.setSelectedDate(selected)

        val s1 = selected.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        val e1 = selected.atTime(11, 0).atZone(zoneId).toInstant().toEpochMilli()

        flow.emit(

            listOf(

                Appointment("Doctor Visit", "bring reports", s1, e1),

                Appointment("Meeting", "project discussion", s1 + 120_000, e1 + 120_000)

            )

        )

        advanceUntilIdle()

        vm.setSearchQuery("doctor")

        val result = vm.appointmentsForSelectedDay()

        assertEquals(1, result.size)

        assertEquals("Doctor Visit", result[0].title)

    }

    @Test

    fun appointmentsForSelectedDay_filterUpcomingAndPast() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.setUser("uid123")

        val selected = LocalDate.of(2026, 1, 10)

        vm.setSelectedDate(selected)

        val farPastStart = 1_000L

        val farPastEnd = 2_000L

        val farFutureStart = 4_000_000_000_000L

        val farFutureEnd = 4_000_000_000_100L

        // Put them on the selected date in UTC

        val dayStart = selected.atStartOfDay(zoneId).toInstant().toEpochMilli()

        val pastStart = dayStart + farPastStart

        val pastEnd = dayStart + farPastEnd

        val futureStart = dayStart + farFutureStart

        val futureEnd = dayStart + farFutureEnd

        flow.emit(

            listOf(

                Appointment("Past", "done", pastStart, pastEnd),

                Appointment("Future", "upcoming", futureStart, futureEnd)

            )

        )

        advanceUntilIdle()

        vm.setFilter(AppointmentFilter.UPCOMING)

        assertEquals(1, vm.appointmentsForSelectedDay().size)

        assertEquals("Future", vm.appointmentsForSelectedDay()[0].title)

        vm.setFilter(AppointmentFilter.PAST)

        assertEquals(1, vm.appointmentsForSelectedDay().size)

        assertEquals("Past", vm.appointmentsForSelectedDay()[0].title)

    }

    @Test

    fun addAppointment_callsRepo_whenUserIsSet() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

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

            }

        )

    }

    @Test

    fun addAppointment_doesNothing_whenUserIdBlank() = runTest {

        val vm = AppointmentViewModel(repo, zoneId)

        vm.addAppointment("Title", "Note", 10L, 20L)

        advanceUntilIdle()

        verify(repo, never()).addAppointment(any(), any())

    }

}

