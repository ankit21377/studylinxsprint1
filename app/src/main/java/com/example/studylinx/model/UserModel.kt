package com.example.studylinx.model

data class UserModel(
    val userId: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUrl: String = "",

    // ✅ NEW FIELDS (safe defaults)
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val address: String = "",
    val interestedCourseOrCountry: String = ""
) {

    fun fullName(): String = "$firstname $lastname".trim()

    // ✅ KEEP your existing keys to avoid breaking other screens
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "firstName" to firstname,
            "lastName" to lastname,
            "email" to email,
            "profileImageUrl" to profileImageUrl,

            // ✅ NEW KEYS
            "phoneNumber" to phoneNumber,
            "dateOfBirth" to dateOfBirth,
            "address" to address,
            "interestedCourseOrCountry" to interestedCourseOrCountry
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
                profileImageUrl = map["profileImageUrl"] as? String ?: "",

                phoneNumber = map["phoneNumber"] as? String ?: "",
                dateOfBirth = map["dateOfBirth"] as? String ?: "",
                address = map["address"] as? String ?: "",
                interestedCourseOrCountry = map["interestedCourseOrCountry"] as? String ?: ""
            )
        }
    }
}