package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.core.CourseKey
import com.example.studylinx.model.Course
import com.example.studylinx.model.University
import com.example.studylinx.repo.CourseIndexRepo
import com.example.studylinx.repo.CourseIndexRepoImpl
import com.example.studylinx.repo.CourseRepo
import com.example.studylinx.repo.CourseRepoImpl
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminCoursesUiState(
    val loading: Boolean = true,
    val courses: List<Course> = emptyList(),
    val error: String? = null,
    val toast: String? = null
)

class AdminCoursesViewModel(
    private val repo: CourseRepo = CourseRepoImpl(),
    private val indexRepo: CourseIndexRepo = CourseIndexRepoImpl(),
) : ViewModel() {

    private val root = FirebaseDatabase.getInstance().reference
    private val universitiesRef = root.child("universities")

    private val _state = MutableStateFlow(AdminCoursesUiState())
    val state: StateFlow<AdminCoursesUiState> = _state

    init {
        viewModelScope.launch {
            runCatching {
                repo.observeCourses().collect { list ->
                    _state.value = AdminCoursesUiState(
                        loading = false,
                        courses = list,
                        error = null,
                        toast = null
                    )
                }
            }.onFailure { e ->
                _state.value = AdminCoursesUiState(
                    loading = false,
                    courses = emptyList(),
                    error = e.message ?: "Failed to load courses",
                    toast = null
                )
            }
        }
    }

    fun clearToast() {
        _state.value = _state.value.copy(toast = null)
    }

    fun addCourse(name: String) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true)
                repo.addCourse(name)
            }.onSuccess {
                _state.value = _state.value.copy(loading = false, toast = "Course added")
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, toast = e.message ?: "Failed")
            }
        }
    }

    fun updateCourse(old: Course, newName: String) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true)

                val oldKey = CourseKey.keyOf(old.name)
                val newKey = CourseKey.keyOf(newName)

                // 1) Update course name in courses node
                repo.updateCourse(old.id, newName)

                // 2) Move course_index (rename node):
                // course_index/oldKey/{uniId}=true -> course_index/newKey/{uniId}=true
                if (oldKey != newKey) {
                    val idsSnap = root.child("course_index").child(oldKey).get().await()
                    val ids = idsSnap.children.mapNotNull { it.key }

                    // copy to new
                    for (id in ids) indexRepo.addUniversityToCourse(newKey, id)
                    // remove old
                    indexRepo.deleteCourseIndex(oldKey)

                    // 3) Update course name in every university.courses list
                    val uniSnap = universitiesRef.get().await()
                    for (u in uniSnap.children) {
                        val uniId = u.key ?: continue
                        val model = u.getValue(University::class.java) ?: continue
                        val updatedCourses = model.courses.map {
                            if (CourseKey.keyOf(it) == oldKey) newName else it
                        }
                        universitiesRef.child(uniId).child("courses").setValue(updatedCourses).await()
                    }
                }

            }.onSuccess {
                _state.value = _state.value.copy(loading = false, toast = "Course updated")
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, toast = e.message ?: "Failed")
            }
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true)

                val key = CourseKey.keyOf(course.name)

                // 1) Delete course from courses node
                repo.deleteCourse(course.id)

                // 2) Delete course index node
                indexRepo.deleteCourseIndex(key)

                // 3) Remove course from every university
                val uniSnap = universitiesRef.get().await()
                for (u in uniSnap.children) {
                    val uniId = u.key ?: continue
                    val model = u.getValue(University::class.java) ?: continue
                    if (model.courses.any { CourseKey.keyOf(it) == key }) {
                        val updated = model.courses.filterNot { CourseKey.keyOf(it) == key }
                        universitiesRef.child(uniId).child("courses").setValue(updated).await()
                    }
                }
            }.onSuccess {
                _state.value = _state.value.copy(loading = false, toast = "Course deleted")
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, toast = e.message ?: "Failed")
            }
        }
    }
}
