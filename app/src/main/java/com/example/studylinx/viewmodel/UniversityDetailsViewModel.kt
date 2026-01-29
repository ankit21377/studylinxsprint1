package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.University
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UniversityDetailsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val university: University? = null,
    val enrolling: Boolean = false
)

class UniversityDetailsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val uniRef = FirebaseDatabase.getInstance().getReference("Universities")
    private val enrollRef = FirebaseDatabase.getInstance().getReference("Enrollments")

    private val _ui = MutableStateFlow(UniversityDetailsUiState())
    val ui: StateFlow<UniversityDetailsUiState> = _ui

    fun loadUniversity(uniId: String) {
        if (uniId.isBlank()) {
            _ui.value = UniversityDetailsUiState(loading = false, error = "Invalid university id")
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null)

        uniRef.child(uniId).get()
            .addOnSuccessListener { snap ->
                val uni = snap.getValue(University::class.java)
                _ui.value = _ui.value.copy(loading = false, university = uni, error = null)
            }
            .addOnFailureListener { e ->
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Failed to load")
            }
    }

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
                onDone(true, "Enrollment saved ✅")
            }
            .addOnFailureListener { e ->
                _ui.value = _ui.value.copy(enrolling = false)
                onDone(false, e.message ?: "Failed to enroll")
            }
    }
}