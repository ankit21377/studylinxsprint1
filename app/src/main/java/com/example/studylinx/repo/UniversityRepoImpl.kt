package com.example.studylinx.repo

import com.example.studylinx.model.University
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UniversityRepoImpl(
    private val ref: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("universities") // ✅ lowercase
) : UniversityRepo {

    override fun observeUniversities(): Flow<List<University>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { snap ->
                    snap.getValue(University::class.java)?.copy(id = snap.key ?: "")
                }
                trySend(list).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun getUniversityById(id: String): University? {
        if (id.isBlank()) return null
        val snap = ref.child(id).get().await()
        return snap.getValue(University::class.java)?.copy(id = id)
    }
}
