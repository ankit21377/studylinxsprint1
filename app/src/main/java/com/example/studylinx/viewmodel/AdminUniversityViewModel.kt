package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.University
import com.example.studylinx.repo.UniversityRepo
import com.example.studylinx.repo.UniversityRepoImpl
import com.example.studylinx.viewmodel.AdminUniversityUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminUniversityViewModel(
    private val repo: UniversityRepo = UniversityRepoImpl()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUniversityUiState())
    val state: StateFlow<AdminUniversityUiState> = _state

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onCity(v: String) = _state.update { it.copy(city = v) }
    fun onCountry(v: String) = _state.update { it.copy(country = v) }
    fun onDesc(v: String) = _state.update { it.copy(description = v) }
    fun onImage(v: String) = _state.update { it.copy(imageUrl = v) }
    fun onLocation(v: String) = _state.update { it.copy(locationUrl = v) }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun saveUniversity() {
        val s = _state.value

        if (s.name.isBlank() || s.city.isBlank() || s.country.isBlank() || s.description.isBlank()) {
            _state.update { it.copy(message = "Fill Name, City, Country & Description") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null) }

            try {
                repo.addUniversity(
                    University(
                        name = s.name.trim(),
                        city = s.city.trim(),
                        country = s.country.trim(),
                        description = s.description.trim(),
                        imageUrl = s.imageUrl.trim(),
                        locationUrl = s.locationUrl.trim()
                    )
                )

                _state.update {
                    AdminUniversityUiState(message = "Saved successfully ✅")
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(saving = false, message = "Save failed: ${e.message}")
                }
            }
        }
    }
}
