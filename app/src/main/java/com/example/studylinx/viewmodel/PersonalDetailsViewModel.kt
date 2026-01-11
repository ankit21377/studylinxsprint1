package com.example.studylinx.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.studylinx.model.UserModel
import com.example.studylinx.repo.UserRepo
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

data class PersonalDetailsUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val message: String? = null,
    val user: UserModel = UserModel(),
    val pickedImageUri: Uri? = null
)

class PersonalDetailsViewModel(
    private val repo: UserRepo
) : ViewModel() {

    var state: PersonalDetailsUiState = PersonalDetailsUiState()
        private set

    private fun updateState(newState: PersonalDetailsUiState, emit: (PersonalDetailsUiState) -> Unit) {
        state = newState
        emit(state)
    }

    private fun uid(): String = repo.getCurrentUser()?.uid.orEmpty()

    fun loadUser(emit: (PersonalDetailsUiState) -> Unit) {
        val id = uid()
        if (id.isBlank()) {
            updateState(state.copy(loading = false, message = "User not logged in"), emit)
            return
        }

        updateState(state.copy(loading = true, message = null), emit)

        repo.getUserById(id) { ok, msg, user ->
            if (ok && user != null) {
                updateState(state.copy(loading = false, user = user, message = null), emit)
            } else {
                updateState(state.copy(loading = false, message = msg.ifBlank { "Failed to load user" }), emit)
            }
        }
    }

    fun setPickedImage(uri: Uri?, emit: (PersonalDetailsUiState) -> Unit) {
        updateState(state.copy(pickedImageUri = uri), emit)
    }

    // Field setters (update inside UserModel)
    fun setFirstName(v: String, emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(user = state.user.copy(firstname = v)), emit)

    fun setLastName(v: String, emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(user = state.user.copy(lastname = v)), emit)

    fun setEmail(v: String, emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(user = state.user.copy(email = v)), emit)

    fun setPhone(v: String, emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(user = state.user.copy(phoneNumber = v)), emit)

    fun setDob(v: String, emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(user = state.user.copy(dateOfBirth = v)), emit)

    fun setAddress(v: String, emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(user = state.user.copy(address = v)), emit)

    fun setInterested(v: String, emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(user = state.user.copy(interestedCourseOrCountry = v)), emit)

    fun clearMessage(emit: (PersonalDetailsUiState) -> Unit) =
        updateState(state.copy(message = null), emit)

    fun saveDetails(emit: (PersonalDetailsUiState) -> Unit) {
        val id = uid()
        if (id.isBlank()) {
            updateState(state.copy(message = "User not logged in"), emit)
            return
        }

        updateState(state.copy(saving = true, message = null), emit)

        val uri = state.pickedImageUri
        if (uri == null) {
            // Save without image upload
            repo.updateProfile(id, state.user.copy(userId = id)) { ok, msg ->
                updateState(
                    state.copy(
                        saving = false,
                        message = if (ok) "Saved successfully" else msg.ifBlank { "Save failed" }
                    ),
                    emit
                )
            }
            return
        }

        // Upload image then save
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("users/$id/profile/${UUID.randomUUID()}.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        val updatedUser = state.user.copy(
                            userId = id,
                            profileImageUrl = downloadUrl.toString()
                        )

                        repo.updateProfile(id, updatedUser) { ok, msg ->
                            updateState(
                                state.copy(
                                    saving = false,
                                    user = if (ok) updatedUser else state.user,
                                    pickedImageUri = null,
                                    message = if (ok) "Saved successfully" else msg.ifBlank { "Save failed" }
                                ),
                                emit
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        updateState(state.copy(saving = false, message = e.message ?: "Failed to get image URL"), emit)
                    }
            }
            .addOnFailureListener { e ->
                updateState(state.copy(saving = false, message = e.message ?: "Image upload failed"), emit)
            }
    }
}