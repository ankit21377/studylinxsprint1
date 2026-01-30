package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.University
import com.example.studylinx.repo.UniversityRepo
import com.example.studylinx.repo.UniversityRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ✅ UI STATE INSIDE VIEWMODEL FILE
data class UniversityDetailsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val university: University? = null,
    val enrolling: Boolean = false
)

class UniversityDetailsViewModel(
    private val repo: UniversityRepo = UniversityRepoImpl()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val enrollRef = FirebaseDatabase.getInstance().reference.child("enrollments")

    private val _ui = MutableStateFlow(UniversityDetailsUiState())
    val ui: StateFlow<UniversityDetailsUiState> = _ui

    // ---------------- LOAD UNIVERSITY ----------------
    fun loadUniversity(uniId: String) {
        if (uniId.isBlank()) {
            _ui.value = UniversityDetailsUiState(
                loading = false,
                error = "Invalid university id"
            )
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null)

        viewModelScope.launch {
            runCatching {
                repo.getUniversityById(uniId)
            }.onSuccess { uni ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    university = uni,
                    error = null
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load university"
                )
            }
        }
    }

    // ---------------- ENROLL USER ----------------
    fun enroll(uniId: String, onDone: (Boolean, String) -> Unit) {
        val userId = auth.currentUser?.uid ?: ""

        if (userId.isBlank()) {
            onDone(false, "Please login first")
            return
        }
        if (uniId.isBlank()) {
            onDone(false, "Invalid university")
            return
        }

        _ui.value = _ui.value.copy(enrolling = true)

        val data = mapOf(
            "uniId" to uniId,
            "userId" to userId,
            "enrolledAt" to System.currentTimeMillis()
        )

        enrollRef.child(userId).child(uniId).setValue(data)
            .addOnSuccessListener {
                _ui.value = _ui.value.copy(enrolling = false)
                onDone(true, "Enrollment successful ✅")
            }
            .addOnFailureListener { e ->
                _ui.value = _ui.value.copy(enrolling = false)
                onDone(false, e.message ?: "Enrollment failed")
            }
    }
}
