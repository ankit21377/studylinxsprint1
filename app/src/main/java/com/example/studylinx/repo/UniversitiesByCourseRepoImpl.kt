// File: com/example/studylinx/repo/UniversitiesByCourseRepoImpl.kt
package com.example.studylinx.repo

import com.example.studylinx.model.University
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UniversitiesByCourseRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UniversitiesByCourseRepo {

    override suspend fun getUniversitiesByCourse(courseName: String): List<University> {
        if (courseName.isBlank()) return emptyList()

        val snap = db.collection("universities")
            .whereArrayContains("courses", courseName) // ✅ ONLY universities with that course
            .get()
            .await()

        return snap.documents.map { d ->
            University(
                id = d.id,
                name = d.getString("name") ?: "",
                city = d.getString("city") ?: "",
                country = d.getString("country") ?: "",
                description = d.getString("description") ?: "",
                imageUrl = d.getString("imageUrl") ?: "",
                locationUrl = d.getString("locationUrl") ?: "",
                courses = (d.get("courses") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }
}
