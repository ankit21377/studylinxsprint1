package com.example.studylinx.repo

import android.net.Uri
import com.example.studylinx.core.CourseKey
import com.example.studylinx.model.University
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AdminUniversityRepoImpl(
    private val root: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val indexRepo: CourseIndexRepo = CourseIndexRepoImpl(root)
) : AdminUniversityRepo {

    private val uniRef = root.child("universities")

    override suspend fun uploadUniversityImage(uniId: String, imageUri: Uri): String {
        val path = "university_images/$uniId/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(path)
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun addUniversity(u: University): String {
        val uniId = uniRef.push().key ?: throw IllegalStateException("No key")
        val cleanCourses = u.courses.map { it.trim() }.filter { it.isNotBlank() }.distinct()

        val data = u.copy(id = uniId, courses = cleanCourses)
        uniRef.child(uniId).setValue(data).await()

        // ✅ write course index
        for (c in cleanCourses) {
            val key = CourseKey.keyOf(c)
            indexRepo.addUniversityToCourse(key, uniId)
        }
        return uniId
    }

    override suspend fun updateUniversity(uniId: String, updated: University) {
        // Fetch old courses to diff
        val oldSnap = uniRef.child(uniId).get().await()
        val old = oldSnap.getValue(University::class.java) ?: University(id = uniId)
        val oldCourses = old.courses.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        val newCourses = updated.courses.map { it.trim() }.filter { it.isNotBlank() }.distinct()

        // Save university
        uniRef.child(uniId).setValue(updated.copy(id = uniId, courses = newCourses)).await()

        // Diff indexes
        val removed = oldCourses.toSet() - newCourses.toSet()
        val added = newCourses.toSet() - oldCourses.toSet()

        for (c in removed) indexRepo.removeUniversityFromCourse(CourseKey.keyOf(c), uniId)
        for (c in added) indexRepo.addUniversityToCourse(CourseKey.keyOf(c), uniId)
    }

    override suspend fun deleteUniversity(uniId: String) {
        // Need old courses to remove index
        val snap = uniRef.child(uniId).get().await()
        val old = snap.getValue(University::class.java)
        val courses = old?.courses?.map { it.trim() }?.filter { it.isNotBlank() }?.distinct().orEmpty()

        // delete university node
        uniRef.child(uniId).removeValue().await()

        // delete from indexes
        for (c in courses) {
            indexRepo.removeUniversityFromCourse(CourseKey.keyOf(c), uniId)
        }
    }
}
