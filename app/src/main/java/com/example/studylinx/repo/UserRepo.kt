package com.example.studylinx.repo

import android.net.Uri
import com.example.studylinx.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepo {

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit)

    fun forgetpassword(email: String, callback: (Boolean, String) -> Unit)

    fun updateProfile(userId: String, model: UserModel, callback: (Boolean, String) -> Unit)

    fun getCurrentUser(): FirebaseUser?

    fun deleteProfile(userId: String, callback: (Boolean, String) -> Unit)

    fun logout(callback: (Boolean, String) -> Unit)

    fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit)

    fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit)

    fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit)

    fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String) -> Unit)

    // ✅ NEW: upload profile image (device -> Firebase Storage -> download url)
    fun uploadProfileImage(
        userId: String,
        imageUri: Uri,
        callback: (Boolean, String, String?) -> Unit
    )

    // ✅ NEW: save personal details (merge into Users/{uid})
    fun savePersonalDetails(
        userId: String,
        fullName: String,
        email: String,
        phone: String,
        dob: String,
        address: String,
        interested: String,
        profileImageUrl: String,
        callback: (Boolean, String) -> Unit
    )
}