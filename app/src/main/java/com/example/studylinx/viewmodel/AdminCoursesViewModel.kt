// File: com/example/studylinx/viewmodel/AdminCoursesViewModel.kt
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.Course
import com.example.studylinx.repo.AdminCourseSyncRepo
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminCoursesState(
    val loading: Boolean = false,
    val error: String? = null,
    val courses: List<Course> = emptyList(),
    val toast: String? = null
)

class AdminCoursesViewModel : ViewModel() {

    private val syncRepo = AdminCourseSyncRepo()
    private val coursesRef = FirebaseDatabase.getInstance().reference.child("courses")

    private val _state = MutableStateFlow(AdminCoursesState(loading = true))
    val state: StateFlow<AdminCoursesState> = _state

    init {
        loadCourses()
    }

    fun clearToast() {
        _state.value = _state.value.copy(toast = null)
    }

    fun loadCourses() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val snap = coursesRef.get().await()
                val list = snap.children.mapNotNull { c ->
                    val id = c.child("id").getValue(String::class.java) ?: c.key ?: return@mapNotNull null
                    val name = c.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    Course(id = id, name = name)
                }.sortedBy { it.name.lowercase() }

                _state.value = _state.value.copy(loading = false, courses = list)
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Failed to load")
            }
        }
    }

    fun addCourse(name: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                syncRepo.addCourse(name.trim())
            }.onSuccess {
                _state.value = _state.value.copy(toast = "Course added")
                loadCourses()
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Failed to add")
            }
        }
    }

    fun updateCourse(target: Course, newName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                syncRepo.renameCourse(target.id, target.name, newName.trim())
            }.onSuccess {
                _state.value = _state.value.copy(toast = "Course updated")
                loadCourses()
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Failed to update")
            }
        }
    }

    fun deleteCourse(target: Course) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                syncRepo.deleteCourse(target.id, target.name)
            }.onSuccess {
                _state.value = _state.value.copy(toast = "Course deleted")
                loadCourses()
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "Failed to delete")
            }
        }
    }
}
