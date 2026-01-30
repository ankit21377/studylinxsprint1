package com.example.studylinx.repo

import android.net.Uri
import com.example.studylinx.model.University
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AdminUniversityRepoImpl(
    private val root: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : AdminUniversityRepo {

    private val uniRef = root.child("universities")

    override suspend fun uploadUniversityImage(uniId: String, imageUri: Uri): String {
        val path = "university_images/$uniId/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(path)
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun addUniversity(u: University): String {
        val uniId = uniRef.push().key ?: throw IllegalStateException("Failed to generate key")
        val clean = u.copy(
            id = uniId,
            name = u.name.trim(),
            city = u.city.trim(),
            country = u.country.trim(),
            countryId = u.countryId.trim(),
            description = u.description.trim(),
            locationUrl = u.locationUrl.trim(),
            courses = u.courses.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        )
        uniRef.child(uniId).setValue(clean).await()
        return uniId
    }

    override suspend fun updateUniversity(uniId: String, updated: University) {
        val clean = updated.copy(
            id = uniId,
            name = updated.name.trim(),
            city = updated.city.trim(),
            country = updated.country.trim(),
            countryId = updated.countryId.trim(),
            description = updated.description.trim(),
            locationUrl = updated.locationUrl.trim(),
            courses = updated.courses.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        )
        uniRef.child(uniId).setValue(clean).await()
    }

    override suspend fun deleteUniversity(uniId: String) {
        uniRef.child(uniId).removeValue().await()
        // optional: delete images from storage (not required for app correctness)
    }
}
