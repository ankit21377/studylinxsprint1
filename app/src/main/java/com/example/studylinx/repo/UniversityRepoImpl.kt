package com.example.studylinx.repo

import com.example.studylinx.model.University
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UniversityRepoImpl(
    private val db: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("universities")
) : UniversityRepo {

    override fun observeUniversities(): Flow<List<University>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { snap ->
                    snap.getValue(University::class.java)
                        ?.copy(id = snap.key ?: "")
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        db.addValueEventListener(listener)
        awaitClose { db.removeEventListener(listener) }
    }

    override suspend fun addUniversity(u: University) {
        suspendCancellableCoroutine<Unit> { cont ->
            val key = db.push().key ?: return@suspendCancellableCoroutine
            val data = u.copy(id = key)

            db.child(key).setValue(data)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }
}
