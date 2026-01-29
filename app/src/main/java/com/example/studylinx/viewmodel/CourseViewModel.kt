package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.repo.CourseRepo
import com.example.studylinx.repo.CourseRepoImpl
import com.example.studylinx.model.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CoursesUiState(
    val loading: Boolean = true,
    val courses: List<Course> = emptyList(),
    val error: String? = null
)

class CoursesViewModel(
    private val repo: CourseRepo = CourseRepoImpl()
) : ViewModel() {

    private val _state = MutableStateFlow(CoursesUiState())
    val state: StateFlow<CoursesUiState> = _state

    init {
        viewModelScope.launch {
            runCatching {
                repo.observeCourses().collect { list ->
                    _state.value = CoursesUiState(loading = false, courses = list, error = null)
                }
            }.onFailure { e ->
                _state.value = CoursesUiState(loading = false, courses = emptyList(), error = e.message)
            }
        }
    }
}
