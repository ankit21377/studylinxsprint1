package com.example.studylinx.model

data class UserModel(
    val userId: String = "",
    val firstname: String = "",
    val lastname:String = "",
    val email:String = "",
    val password: String ="",
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "userId" to userId,
            "firstName" to firstname,
            "lastName" to lastname,
            "email" to email,
        )
    }

}
