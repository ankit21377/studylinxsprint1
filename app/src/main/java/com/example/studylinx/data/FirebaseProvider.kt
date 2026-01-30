package com.example.studylinx.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object FirebaseProvider {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val db by lazy { FirebaseDatabase.getInstance().reference }

    // Storage
    val storage by lazy { FirebaseStorage.getInstance().reference }

    // Paths
    fun usersRef() = db.child("users")
    fun countriesRef() = db.child("countries")
    fun coursesRef() = db.child("courses")
    fun universitiesRef() = db.child("universities")
    fun appointmentsRef(uid: String) = db.child("appointments").child(uid)
    fun notificationsRef(uid: String) = db.child("notifications").child(uid)
    fun documentsRef(uid: String) = db.child("documents").child(uid)
    fun enrollmentsRef(uid: String) = db.child("enrollments").child(uid)
}
