package com.example.studylinx.repo

import com.example.studylinx.model.University
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class UniversityReadRepoImpl(
    private val root: DatabaseReference = FirebaseDatabase.getInstance().reference
) : UniversityReadRepo {

    private val uniRef = root.child("universities")

    override suspend fun getUniversitiesByIds(ids: List<String>): List<University> {
        if (ids.isEmpty()) return emptyList()

        // For RTDB, we fetch each id (ok for medium lists; scalable enough)
        val result = mutableListOf<University>()
        for (id in ids) {
            val snap = uniRef.child(id).get().await()
            val u = snap.getValue(University::class.java)
            if (u != null) result.add(u.copy(id = id))
        }
        return result.sortedBy { it.name.lowercase() }
    }
}
