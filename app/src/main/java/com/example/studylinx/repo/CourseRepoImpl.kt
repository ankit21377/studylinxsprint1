package com.example.studylinx.repo

import com.example.studylinx.model.Course
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CourseRepoImpl(
    private val root: DatabaseReference = FirebaseDatabase.getInstance().reference
) : CourseRepo {

    private val coursesRef = root.child("courses")

    override fun observeCourses(): Flow<List<Course>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { s ->
                    s.getValue(Course::class.java)?.copy(id = s.key ?: "")
                }.sortedBy { it.name.lowercase() }
                trySend(list).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        coursesRef.addValueEventListener(listener)
        awaitClose { coursesRef.removeEventListener(listener) }
    }

    override suspend fun addCourse(name: String): String {
        val key = coursesRef.push().key ?: throw IllegalStateException("No key")
        val now = System.currentTimeMillis()
        val course = Course(id = key, name = name.trim(), createdAt = now)
        coursesRef.child(key).setValue(course).await()
        return key
    }

    override suspend fun updateCourse(courseId: String, newName: String) {
        coursesRef.child(courseId).child("name").setValue(newName.trim()).await()
    }

    override suspend fun deleteCourse(courseId: String) {
        coursesRef.child(courseId).removeValue().await()
    }
}
