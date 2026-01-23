package com.example.studylinx.repo

import android.net.Uri
import com.example.studylinx.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val ref: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")

    private val storage = FirebaseStorage.getInstance()

    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Login successfully")
                else callback(false, it.exception?.message ?: "Login failed")
            }
    }

    override fun forgetpassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Reset link sent to $email")
            else callback(false, it.exception?.message ?: "Failed to send reset link")
        }
    }

    override fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    callback(true, "Registration success", uid)
                } else {
                    callback(false, it.exception?.message ?: "Registration failed", "")
                }
            }
    }

    override fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "User added successfully")
            else callback(false, it.exception?.message ?: "Failed to save user")
        }
    }

    override fun updateProfile(userId: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        ref.child(userId).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Profile updated successfully")
            else callback(false, it.exception?.message ?: "Update failed")
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun deleteProfile(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                auth.currentUser?.delete()
                callback(true, "Account deleted")
            } else callback(false, it.exception?.message ?: "Delete failed")
        }
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout successful")
        } catch (e: Exception) {
            callback(false, e.message ?: "Logout failed")
        }
    }

    override fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
        ref.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)
                    callback(true, "Profile fetched", user)
                } else callback(false, "User not found", null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allUsers = mutableListOf<UserModel>()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val user = data.getValue(UserModel::class.java)
                        if (user != null) allUsers.add(user)
                    }
                }
                callback(true, "Users fetched", allUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    // ✅ Upload profile image to Firebase Storage and return download URL
    override fun uploadProfileImage(
        userId: String,
        imageUri: Uri,
        callback: (Boolean, String, String?) -> Unit
    ) {
        val imgRef = storage.reference.child("profileImages/$userId/profile_${System.currentTimeMillis()}.jpg")
        imgRef.putFile(imageUri)
            .addOnSuccessListener {
                imgRef.downloadUrl
                    .addOnSuccessListener { url -> callback(true, "Image uploaded", url.toString()) }
                    .addOnFailureListener { e -> callback(false, e.message ?: "Failed to get image url", null) }
            }
            .addOnFailureListener { e -> callback(false, e.message ?: "Image upload failed", null) }
    }

    // ✅ Save personal details into RTDB Users/{uid} (merge/partial update)
    override fun savePersonalDetails(
        userId: String,
        fullName: String,
        email: String,
        phone: String,
        dob: String,
        address: String,
        interested: String,
        profileImageUrl: String,
        callback: (Boolean, String) -> Unit
    ) {
        val parts = fullName.trim().split(" ")
        val first = parts.firstOrNull().orEmpty()
        val last = parts.drop(1).joinToString(" ").trim()

        val map = hashMapOf<String, Any?>(
            "userId" to userId,
            "firstName" to first,
            "lastName" to last,
            "email" to email.trim(),
            "phoneNumber" to phone.trim(),
            "dateOfBirth" to dob.trim(),
            "address" to address.trim(),
            "interestedCourseOrCountry" to interested.trim(),
            "profileImageUrl" to profileImageUrl.trim()
        )

        ref.child(userId).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Personal details saved")
            else callback(false, it.exception?.message ?: "Failed to save details")
        }
    }
}