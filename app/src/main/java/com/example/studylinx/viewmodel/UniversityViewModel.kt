package com.example.studylinx.university.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.University
import com.example.studylinx.repo.UniversityRepo
import com.example.studylinx.repo.UniversityRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UniversityViewModel(
    private val repo: UniversityRepo = UniversityRepoImpl()
) : ViewModel() {

    private val _state = MutableStateFlow(UniversityUiState())
    val state: StateFlow<UniversityUiState> = _state

    init {
        viewModelScope.launch {
            repo.observeUniversities().collect { list ->
                _state.update {
                    it.copy(
                        loading = false,
                        all = list,
                        filtered = applyFilter(list, it.query)
                    )
                }
            }
        }
    }

    fun onQueryChange(q: String) {
        _state.update {
            it.copy(query = q, filtered = applyFilter(it.all, q))
        }
    }

    private fun applyFilter(list: List<University>, q: String): List<University> {
        val query = q.trim().lowercase()
        if (query.isEmpty()) return list
        return list.filter {
            it.name.lowercase().contains(query) ||
                    it.city.lowercase().contains(query) ||
                    it.country.lowercase().contains(query)
        }
    }
}
