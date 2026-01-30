// File: com/example/studylinx/repo/UniversitiesByCourseRepo.kt
package com.example.studylinx.repo

import com.example.studylinx.model.University

interface UniversitiesByCourseRepo {
    suspend fun getUniversitiesByCourse(courseName: String): List<University>
}
