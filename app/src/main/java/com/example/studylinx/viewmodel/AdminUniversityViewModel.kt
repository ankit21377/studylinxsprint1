package com.example.studylinx.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.University
import com.example.studylinx.repo.AdminUniversityRepo
import com.example.studylinx.repo.AdminUniversityRepoImpl
import com.example.studylinx.repo.UniversityRepo
import com.example.studylinx.repo.UniversityRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ✅ UI STATE INSIDE VIEWMODEL FILE
data class AdminUniversityUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val universities: List<University> = emptyList(),

    val query: String = "",
    val toast: String? = null
) {
    val filtered: List<University>
        get() {
            val q = query.trim().lowercase()
            if (q.isBlank()) return universities

            return universities.filter { u ->
                u.name.lowercase().contains(q) ||
                        u.city.lowercase().contains(q) ||
                        u.country.lowercase().contains(q) ||
                        u.courses.any { it.lowercase().contains(q) }
            }
        }
}

class AdminUniversityViewModel(
    private val repo: AdminUniversityRepo = AdminUniversityRepoImpl(),
    private val readRepo: UniversityRepo = UniversityRepoImpl()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUniversityUiState())
    val state: StateFlow<AdminUniversityUiState> = _state.asStateFlow()

    init {
        observeUniversities()
    }

    private fun observeUniversities() {
        viewModelScope.launch {
            readRepo.observeUniversities()
                .collect { list ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = null,
                        universities = list
                    )
                }
        }
    }

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun clearToast() {
        _state.value = _state.value.copy(toast = null)
    }

    // ---------------- ADD UNIVERSITY (with optional image) ----------------
    fun addUniversity(u: University, imageUri: Uri?) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true, error = null)

                // 1) create record first
                val uniId = repo.addUniversity(u)

                // 2) upload image if selected, then update university with imageUrl
                if (imageUri != null) {
                    val url = repo.uploadUniversityImage(uniId, imageUri)
                    repo.updateUniversity(uniId, u.copy(id = uniId, imageUrl = url))
                }

                _state.value = _state.value.copy(
                    loading = false,
                    toast = "University added ✅"
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to add university"
                )
            }
        }
    }

    // ---------------- UPDATE UNIVERSITY (optional new image) ----------------
    fun updateUniversity(uniId: String, updated: University, imageUri: Uri?) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true, error = null)

                val finalUniversity =
                    if (imageUri != null) {
                        val url = repo.uploadUniversityImage(uniId, imageUri)
                        updated.copy(id = uniId, imageUrl = url)
                    } else {
                        updated.copy(id = uniId)
                    }

                repo.updateUniversity(uniId, finalUniversity)

                _state.value = _state.value.copy(
                    loading = false,
                    toast = "University updated ✅"
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to update university"
                )
            }
        }
    }

    // ---------------- DELETE UNIVERSITY ----------------
    fun deleteUniversity(u: University) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true, error = null)

                repo.deleteUniversity(u.id)

                _state.value = _state.value.copy(
                    loading = false,
                    toast = "University deleted ✅"
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to delete university"
                )
            }
        }
    }
}
