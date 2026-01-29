
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import com.example.studylinx.model.UserModel
import com.example.studylinx.repo.UserRepo
import com.google.firebase.auth.FirebaseUser

class UserViewModel(
    private val repo: UserRepo
) : ViewModel() {

    fun register(email: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        repo.register(email, password) { ok, msg, uid ->
            callback(ok, msg, uid)
        }
    }

    fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String?) -> Unit) {
        repo.addUserToDatabase(userId, model) { ok, msg ->
            callback(ok, msg)
        }
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        repo.login(email, password) { ok, msg ->
            callback(ok, msg)
        }
    }

    fun getCurrentUser(): FirebaseUser? = repo.getCurrentUser()

    fun logout(callback: (Boolean, String?) -> Unit) {
        repo.logout { ok, msg -> callback(ok, msg) }
    }

    fun getUserById(userId: String, callback: (Boolean, String?, UserModel?) -> Unit) {
        repo.getUserById(userId) { ok, msg, user ->
            callback(ok, msg, user)
        }
    }
}
