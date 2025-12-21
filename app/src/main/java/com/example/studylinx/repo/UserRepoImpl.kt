package com.example.studylinx.repo

import com.example.studylinx.model.UserModel

class UserRepoImpl : UserRepo{
    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun forgetpassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getCurrentUser(): FirebaseUser {
        TODO("Not yet implemented")
    }

    override fun deleteProfile(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}