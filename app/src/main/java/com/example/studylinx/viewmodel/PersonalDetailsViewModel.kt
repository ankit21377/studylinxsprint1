package com.example.studylinx.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.studylinx.model.UserModel
import com.example.studylinx.repo.UserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PersonalDetailsUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val success: String? = null,

    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dob: String = "",
    val address: String = "",
    val interested: String = "",

    val profileImageUrl: String = "",
    val localImageUri: Uri? = null
)

class PersonalDetailsViewModel(
    private val repo: UserRepo
) : ViewModel() {

    private val _ui = MutableStateFlow(PersonalDetailsUiState())
    val ui: StateFlow<PersonalDetailsUiState> = _ui

    private fun uid(): String = repo.getCurrentUser()?.uid.orEmpty()

    fun loadMe() {
        val userId = uid()
        if (userId.isBlank()) {
            _ui.value = _ui.value.copy(error = "Not logged in")
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null, success = null)

        repo.getUserById(userId) { ok, msg, user ->
            if (!ok || user == null) {
                _ui.value = _ui.value.copy(loading = false, error = msg)
                return@getUserById
            }

            _ui.value = _ui.value.copy(
                loading = false,
                error = null,
                fullName = user.fullName(),
                email = user.email,
                phone = user.phoneNumber,
                dob = user.dateOfBirth,
                address = user.address,
                interested = user.interestedCourseOrCountry,
                profileImageUrl = user.profileImageUrl
            )
        }
    }

    fun setFullName(v: String) = update { it.copy(fullName = v) }
    fun setEmail(v: String) = update { it.copy(email = v) }
    fun setPhone(v: String) = update { it.copy(phone = v) }
    fun setDob(v: String) = update { it.copy(dob = v) }
    fun setAddress(v: String) = update { it.copy(address = v) }
    fun setInterested(v: String) = update { it.copy(interested = v) }

    fun setLocalImage(uri: Uri?) {
        update { it.copy(localImageUri = uri, success = null, error = null) }
    }

    fun saveDetails() {
        val userId = uid()
        if (userId.isBlank()) {
            update { it.copy(error = "Not logged in") }
            return
        }

        val s = _ui.value
        if (s.fullName.trim().isBlank()) { update { it.copy(error = "Full name required") }; return }
        if (s.email.trim().isBlank()) { update { it.copy(error = "Email required") }; return }

        update { it.copy(saving = true, error = null, success = null) }

        // If user picked a new image -> upload it first, then save details with URL
        val picked = s.localImageUri
        if (picked != null) {
            repo.uploadProfileImage(userId, picked) { ok, msg, url ->
                if (!ok || url.isNullOrBlank()) {
                    update { it.copy(saving = false, error = msg) }
                    return@uploadProfileImage
                }
                saveToDbWithUrl(userId, url)
            }
        } else {
            saveToDbWithUrl(userId, s.profileImageUrl)
        }
    }

    private fun saveToDbWithUrl(userId: String, imageUrl: String) {
        val s = _ui.value
        repo.savePersonalDetails(
            userId = userId,
            fullName = s.fullName,
            email = s.email,
            phone = s.phone,
            dob = s.dob,
            address = s.address,
            interested = s.interested,
            profileImageUrl = imageUrl
        ) { ok, msg ->
            if (ok) {
                update { it.copy(saving = false, success = msg, profileImageUrl = imageUrl) }
            } else {
                update { it.copy(saving = false, error = msg) }
            }
        }
    }

    private fun update(block: (PersonalDetailsUiState) -> PersonalDetailsUiState) {
        _ui.value = block(_ui.value)
    }
}