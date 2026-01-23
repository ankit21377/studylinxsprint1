package com.example.studylinx.repo

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CourseIndexRepoImpl(
    private val root: DatabaseReference = FirebaseDatabase.getInstance().reference
) : CourseIndexRepo {

    private val indexRef = root.child("course_index")

    override fun observeUniversityIdsForCourse(courseKey: String): Flow<List<String>> = callbackFlow {
        val ref = indexRef.child(courseKey)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ids = snapshot.children.mapNotNull { it.key }
                trySend(ids).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun addUniversityToCourse(courseKey: String, uniId: String) {
        indexRef.child(courseKey).child(uniId).setValue(true).await()
    }

    override suspend fun removeUniversityFromCourse(courseKey: String, uniId: String) {
        indexRef.child(courseKey).child(uniId).removeValue().await()
    }

    override suspend fun deleteCourseIndex(courseKey: String) {
        indexRef.child(courseKey).removeValue().await()
    }
}
