package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import com.example.studylinx.model.UserModel
import com.example.studylinx.repo.UserRepo
import com.example.studylinx.repo.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProfileUiState(
    val loading: Boolean = true,
    val user: UserModel = UserModel(),
    val error: String? = null
)

class ProfileViewModel(
    private val repo: UserRepo = UserRepoImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui

    fun loadMe() {
        val uid = auth.currentUser?.uid ?: ""
        if (uid.isBlank()) {
            _ui.value = ProfileUiState(loading = false, error = "Not logged in")
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null)

        repo.getUserById(uid) { ok, msg, model ->
            _ui.value = if (ok && model != null) {
                ProfileUiState(loading = false, user = model, error = null)
            } else {
                ProfileUiState(loading = false, user = UserModel(), error = msg)
            }
        }
    }

    fun logout(callback: (Boolean, String) -> Unit) {
        repo.logout(callback)
    }
}