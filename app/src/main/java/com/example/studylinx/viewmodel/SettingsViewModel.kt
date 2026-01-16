
package com.example.studylinx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.core.AppPrefs
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SettingsUiState(
    val darkMode: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext
    private val auth = FirebaseAuth.getInstance()

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui

    init {
        viewModelScope.launch {
            AppPrefs.darkModeFlow(context).collect { enabled ->
                _ui.value = _ui.value.copy(darkMode = enabled)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            AppPrefs.setDarkMode(context, enabled)
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email.isNullOrBlank()) {
            _ui.value = _ui.value.copy(error = "Not logged in")
            return
        }
        if (currentPassword.length < 6) {
            _ui.value = _ui.value.copy(error = "Current password is too short")
            return
        }
        if (newPassword.length < 6) {
            _ui.value = _ui.value.copy(error = "New password must be at least 6 characters")
            return
        }

        _ui.value = _ui.value.copy(saving = true, error = null, message = null)

        viewModelScope.launch {
            runCatching {
                val cred = EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(cred).await()
                user.updatePassword(newPassword).await()
            }.onSuccess {
                _ui.value = _ui.value.copy(
                    saving = false,
                    message = "Password updated successfully ✅",
                    error = null
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    saving = false,
                    error = e.message ?: "Password update failed",
                    message = null
                )
            }
        }
    }

    fun clearBanner() {
        _ui.value = _ui.value.copy(message = null, error = null)
    }
}