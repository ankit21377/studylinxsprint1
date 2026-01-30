// File: com/example/studylinx/repo/AdminCourseSyncRepo.kt
package com.example.studylinx.repo

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AdminCourseSyncRepo {

    private val db = FirebaseDatabase.getInstance().reference

    private val coursesRef = db.child("courses")
    private val courseIndexRef = db.child("course_index")
    private val universitiesRef = db.child("universities")

    suspend fun addCourse(name: String) {
        val id = coursesRef.push().key ?: throw IllegalStateException("Could not generate course id")
        val course = mapOf("id" to id, "name" to name)

        coursesRef.child(id).setValue(course).await()
        courseIndexRef.child(name.lowercase()).setValue(true).await()
    }

    suspend fun renameCourse(courseId: String, oldName: String, newName: String) {
        coursesRef.child(courseId).child("name").setValue(newName).await()

        courseIndexRef.child(oldName.lowercase()).removeValue().await()
        courseIndexRef.child(newName.lowercase()).setValue(true).await()

        // ✅ rename inside ALL universities
        val snap = universitiesRef.get().await()
        for (u in snap.children) {
            val key = u.key ?: continue
            val list = (u.child("courses").value as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            if (list.contains(oldName)) {
                val updated = list.map { if (it == oldName) newName else it }
                universitiesRef.child(key).child("courses").setValue(updated).await()
            }
        }
    }

    suspend fun deleteCourse(courseId: String, courseName: String) {
        coursesRef.child(courseId).removeValue().await()
        courseIndexRef.child(courseName.lowercase()).removeValue().await()

        // ✅ remove from ALL universities
        val snap = universitiesRef.get().await()
        for (u in snap.children) {
            val key = u.key ?: continue
            val list = (u.child("courses").value as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            if (list.contains(courseName)) {
                val updated = list.filter { it != courseName }
                universitiesRef.child(key).child("courses").setValue(updated).await()
            }
        }
    }
}
