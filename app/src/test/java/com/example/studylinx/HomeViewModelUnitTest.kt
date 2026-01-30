package com.example.studylinx

import com.example.studylinx.model.Country
import com.example.studylinx.model.University
import com.example.studylinx.repo.CountryRepo
import com.example.studylinx.repo.UniversityRepo
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

    private lateinit var countryRepo: CountryRepo
    private lateinit var universityRepo: UniversityRepo

    private lateinit var countriesFlow: MutableSharedFlow<List<Country>>
    private lateinit var universitiesFlow: MutableSharedFlow<List<University>>

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        countryRepo = mock()
        universityRepo = mock()

        countriesFlow = MutableSharedFlow(replay = 1)
        universitiesFlow = MutableSharedFlow(replay = 1)

        whenever(countryRepo.observeCountries()).thenReturn(countriesFlow)
        whenever(universityRepo.observeUniversities()).thenReturn(universitiesFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_collectsCountries_updatesUiState() = runTest {
        val vm = HomeViewModel(countryRepo, universityRepo)

        val sample = listOf(
            Country(name = "Nepal"),
            Country(name = "India")
        )

        countriesFlow.emit(sample)
        advanceUntilIdle()

        val state = vm.ui.value
        assertFalse(state.loadingCountries)
        assertEquals(sample, state.countries)
        assertNull(state.errorCountries)
    }

    @Test
    fun init_collectsUniversities_updatesUiState() = runTest {
        val vm = HomeViewModel(countryRepo, universityRepo)

        val sample = listOf(
            University(name = "TU"),
            University(name = "KU")
        )

        universitiesFlow.emit(sample)
        advanceUntilIdle()

        val state = vm.ui.value
        assertFalse(state.loadingUniversities)
        assertEquals(sample, state.universities)
        assertNull(state.errorUniversities)
    }

    @Test
    fun init_collectsBothFlows_updatesBothLists() = runTest {
        val vm = HomeViewModel(countryRepo, universityRepo)

        val sampleCountries = listOf(Country(name = "Nepal"))
        val sampleUniversities = listOf(University(name = "TU"))

        countriesFlow.emit(sampleCountries)
        universitiesFlow.emit(sampleUniversities)

        advanceUntilIdle()

        val state = vm.ui.value
        assertFalse(state.loadingCountries)
        assertFalse(state.loadingUniversities)
        assertEquals(sampleCountries, state.countries)
        assertEquals(sampleUniversities, state.universities)
    }

    @Test
    fun init_callsObserveMethodsOnce() = runTest {
        HomeViewModel(countryRepo, universityRepo)
        advanceUntilIdle()

        verify(countryRepo, times(1)).observeCountries()
        verify(universityRepo, times(1)).observeUniversities()
    }
}