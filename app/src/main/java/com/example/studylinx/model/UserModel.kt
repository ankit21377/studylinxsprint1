package com.example.studylinx.model

data class UserModel(
    val userId: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUrl: String = ""
) {

    fun fullName(): String {
        return "$firstname $lastname".trim()
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "firstName" to firstname,
            "lastName" to lastname,
            "email" to email,
            "profileImageUrl" to profileImageUrl
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): UserModel {
            return UserModel(
                userId = map["userId"] as? String ?: "",
                firstname = map["firstName"] as? String ?: "",
                lastname = map["lastName"] as? String ?: "",
                email = map["email"] as? String ?: "",
                password = "",
                profileImageUrl = map["profileImageUrl"] as? String ?: ""
            )
        }
    }
}
