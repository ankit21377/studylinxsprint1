package com.example.studylinx.repo

import android.net.Uri
import com.example.studylinx.model.University

interface AdminUniversityRepo {
    suspend fun uploadUniversityImage(uniId: String, imageUri: Uri): String

    suspend fun addUniversity(u: University): String
    suspend fun updateUniversity(uniId: String, updated: University)
    suspend fun deleteUniversity(uniId: String)
}
