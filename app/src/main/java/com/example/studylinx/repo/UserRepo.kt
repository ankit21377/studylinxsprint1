package com.example.studylinx.repo

import com.example.studylinx.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepo {

    fun login(email: String, password:String,
              callback: (Boolean, String) -> Unit
    )
    fun forgetpassword(email: String,
                       callback: (Boolean,String) -> Unit
    )
    fun updateProfile(
        userId: String, model: UserModel,
        callback: (Boolean,String) -> Unit
    )
    fun getCurrentUser(): FirebaseUser?

}