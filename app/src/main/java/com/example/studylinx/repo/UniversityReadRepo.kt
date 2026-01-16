package com.example.studylinx.repo

import com.example.studylinx.model.University

interface UniversityReadRepo {
    suspend fun getUniversitiesByIds(ids: List<String>): List<University>
}
