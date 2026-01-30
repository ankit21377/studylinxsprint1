package com.example.studylinx

import com.example.studylinx.model.*
import com.example.studylinx.repo.HomeRepo
import com.example.studylinx.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelUnitTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: HomeRepo

    private lateinit var countriesFlow: MutableSharedFlow<List<Country>>
    private lateinit var universitiesFlow: MutableSharedFlow<List<University>>
    private lateinit var appointmentFlow: MutableSharedFlow<Appointment?>
    private lateinit var progressFlow: MutableSharedFlow<ApplicationProgress>

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        repo = mock()

        countriesFlow = MutableSharedFlow(replay = 1)
        universitiesFlow = MutableSharedFlow(replay = 1)
        appointmentFlow = MutableSharedFlow(replay = 1)
        progressFlow = MutableSharedFlow(replay = 1)

        whenever(repo.observeCountries()).thenReturn(countriesFlow)
        whenever(repo.observeUniversities(any())).thenReturn(universitiesFlow)
        whenever(repo.observeUpcomingAppointment()).thenReturn(appointmentFlow)
        whenever(repo.observeProgress()).thenReturn(progressFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_collectsCountries_updatesUiState() = runTest {
        val vm = HomeViewModel(repo)

        val sample = listOf(Country(name = "Nepal"), Country(name = "India"))
        countriesFlow.emit(sample)
        advanceUntilIdle()

        val state = vm.ui.value
        assertFalse(state.loadingCountries)
        assertEquals(sample, state.countries)
        assertNull(state.errorCountries)
    }

    @Test
    fun init_collectsUniversities_updatesUiState() = runTest {
        val vm = HomeViewModel(repo)

        val sample = listOf(University(name = "TU"), University(name = "KU"))
        universitiesFlow.emit(sample)
        advanceUntilIdle()

        val state = vm.ui.value
        assertFalse(state.loadingUniversities)
        assertEquals(sample, state.universities)
        assertNull(state.errorUniversities)
    }

    @Test
    fun init_callsObserveMethodsOnce() = runTest {
        HomeViewModel(repo)
        advanceUntilIdle()

        verify(repo, times(1)).observeCountries()
        verify(repo, times(1)).observeUniversities(any())
        verify(repo, times(1)).observeUpcomingAppointment()
        verify(repo, times(1)).observeProgress()
    }
}
