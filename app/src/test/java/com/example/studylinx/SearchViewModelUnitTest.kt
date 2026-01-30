package com.example.studylinx

import com.example.studylinx.model.Country
import com.example.studylinx.model.University
import com.example.studylinx.repo.CountryRepo
import com.example.studylinx.repo.UniversityRepo
import com.example.studylinx.viewmodel.SearchViewModel
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
class SearchViewModelUnitTest {

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
    fun countries_areSortedAlphabetically() = runTest {
        val vm = SearchViewModel(countryRepo, universityRepo)

        val list = listOf(
            Country(name = "India"),
            Country(name = "Australia"),
            Country(name = "Nepal")
        )

        countriesFlow.emit(list)
        advanceUntilIdle()

        val state = vm.ui.value
        assertEquals("Australia", state.countries[0].name)
        assertEquals("India", state.countries[1].name)
        assertEquals("Nepal", state.countries[2].name)
    }

    @Test
    fun setQuery_filtersCountriesCorrectly() = runTest {
        val vm = SearchViewModel(countryRepo, universityRepo)

        val list = listOf(
            Country(name = "Nepal"),
            Country(name = "India")
        )

        countriesFlow.emit(list)
        advanceUntilIdle()

        vm.setQuery("ne")
        advanceUntilIdle()

        val filtered = vm.ui.value.filteredCountries
        assertEquals(1, filtered.size)
        assertEquals("Nepal", filtered[0].name)
    }

    @Test
    fun universityQuery_lessThan2Characters_returnsEmptyList() = runTest {
        val vm = SearchViewModel(countryRepo, universityRepo)

        universitiesFlow.emit(
            listOf(
                University(
                    name = "Tribhuvan University",
                    city = "Kathmandu",
                    country = "Nepal",
                    courses = listOf("CS")
                )
            )
        )

        advanceUntilIdle()

        vm.setQuery("n")
        advanceUntilIdle()

        assertTrue(vm.ui.value.filteredUniversities.isEmpty())
    }

    @Test
    fun universityQuery_matchesByCityOrCourse() = runTest {
        val vm = SearchViewModel(countryRepo, universityRepo)

        val uni = University(
            name = "Tribhuvan University",
            city = "Kathmandu",
            country = "Nepal",
            courses = listOf("Computer Science", "Engineering")
        )

        universitiesFlow.emit(listOf(uni))
        advanceUntilIdle()

        vm.setQuery("computer")
        advanceUntilIdle()

        val filtered = vm.ui.value.filteredUniversities
        assertEquals(1, filtered.size)
        assertEquals("Tribhuvan University", filtered[0].name)
    }

    @Test
    fun repositoryThrows_setsError() = runTest {
        whenever(countryRepo.observeCountries()).thenThrow(RuntimeException("Failed"))

        val vm = SearchViewModel(countryRepo, universityRepo)
        advanceUntilIdle()

        assertEquals("Failed", vm.ui.value.error)
    }
}