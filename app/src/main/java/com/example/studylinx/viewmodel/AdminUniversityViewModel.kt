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
import kotlinx.coroutines.launch

data class AdminUniversityUiState(
    val loading: Boolean = true,
    val universities: List<University> = emptyList(),
    val filtered: List<University> = emptyList(),
    val query: String = "",
    val error: String? = null,
    val toast: String? = null
)

class AdminUniversityViewModel(
    private val uniRepo: UniversityRepo = UniversityRepoImpl(),          // observe all
    private val adminRepo: AdminUniversityRepo = AdminUniversityRepoImpl() // add/edit/delete + index sync
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUniversityUiState())
    val state: StateFlow<AdminUniversityUiState> = _state

    init {
        viewModelScope.launch {
            runCatching {
                uniRepo.observeUniversities().collect { list ->
                    val sorted = list.sortedBy { it.name.lowercase() }
                    _state.value = _state.value.copy(
                        loading = false,
                        universities = sorted,
                        filtered = applyFilter(sorted, _state.value.query),
                        error = null
                    )
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load universities"
                )
            }
        }
    }

    fun clearToast() {
        _state.value = _state.value.copy(toast = null)
    }

    fun setQuery(q: String) {
        val list = _state.value.universities
        _state.value = _state.value.copy(
            query = q,
            filtered = applyFilter(list, q)
        )
    }

    private fun applyFilter(list: List<University>, q: String): List<University> {
        val query = q.trim().lowercase()
        if (query.isBlank()) return list
        return list.filter { u ->
            u.name.lowercase().contains(query) ||
                    u.city.lowercase().contains(query) ||
                    u.country.lowercase().contains(query) ||
                    u.courses.any { it.lowercase().contains(query) }
        }
    }

    fun addUniversity(u: University, imageUri: Uri?) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true)

                // first create with placeholder imageUrl
                val id = adminRepo.addUniversity(u.copy(imageUrl = u.imageUrl))

                // upload image if selected
                if (imageUri != null) {
                    val url = adminRepo.uploadUniversityImage(id, imageUri)
                    adminRepo.updateUniversity(id, u.copy(imageUrl = url))
                }
            }.onSuccess {
                _state.value = _state.value.copy(loading = false, toast = "University added")
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, toast = e.message ?: "Failed")
            }
        }
    }

    fun updateUniversity(uniId: String, updated: University, imageUri: Uri?) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true)

                val finalModel =
                    if (imageUri != null) {
                        val url = adminRepo.uploadUniversityImage(uniId, imageUri)
                        updated.copy(imageUrl = url)
                    } else updated

                adminRepo.updateUniversity(uniId, finalModel)
            }.onSuccess {
                _state.value = _state.value.copy(loading = false, toast = "University updated")
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, toast = e.message ?: "Failed")
            }
        }
    }

    fun deleteUniversity(u: University) {
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true)
                adminRepo.deleteUniversity(u.id)
            }.onSuccess {
                _state.value = _state.value.copy(loading = false, toast = "University deleted")
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, toast = e.message ?: "Failed")
            }
        }
    }
}
