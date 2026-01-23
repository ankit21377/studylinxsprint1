package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.core.CourseKey
import com.example.studylinx.model.University
import com.example.studylinx.repo.CourseIndexRepo
import com.example.studylinx.repo.CourseIndexRepoImpl
import com.example.studylinx.repo.UniversityReadRepo
import com.example.studylinx.repo.UniversityReadRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UniversitiesByCourseUiState(
    val loading: Boolean = true,
    val courseName: String = "",
    val list: List<University> = emptyList(),
    val error: String? = null
)

class UniversitiesByCourseViewModel(
    private val indexRepo: CourseIndexRepo = CourseIndexRepoImpl(),
    private val uniRepo: UniversityReadRepo = UniversityReadRepoImpl()
) : ViewModel() {

    private val _state = MutableStateFlow(UniversitiesByCourseUiState())
    val state: StateFlow<UniversitiesByCourseUiState> = _state

    fun start(courseName: String) {
        val key = CourseKey.keyOf(courseName)
        _state.value = UniversitiesByCourseUiState(loading = true, courseName = courseName)

        viewModelScope.launch {
            runCatching {
                indexRepo.observeUniversityIdsForCourse(key).collect { ids ->
                    val universities = uniRepo.getUniversitiesByIds(ids)
                    _state.value = _state.value.copy(loading = false, list = universities, error = null)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }
}
