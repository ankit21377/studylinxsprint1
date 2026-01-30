// File: com/example/studylinx/viewmodel/UniversitiesByCourseViewModel.kt
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.University
import com.example.studylinx.repo.UniversitiesByCourseRepo
import com.example.studylinx.repo.UniversitiesByCourseRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UniversitiesByCourseViewModel(
    private val repo: UniversitiesByCourseRepo = UniversitiesByCourseRepoImpl()
) : ViewModel() {

    data class State(
        val loading: Boolean = false,
        val error: String? = null,
        val list: List<University> = emptyList()
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun start(courseName: String) {
        if (courseName.isBlank()) {
            _state.value = State(loading = false, error = "Course name missing", list = emptyList())
            return
        }

        _state.value = _state.value.copy(loading = true, error = null)

        viewModelScope.launch {
            runCatching {
                repo.getUniversitiesByCourse(courseName)
            }.onSuccess { list ->
                _state.value = State(loading = false, error = null, list = list)
            }.onFailure { e ->
                _state.value = State(loading = false, error = e.message ?: "Failed to load universities", list = emptyList())
            }
        }
    }
}
