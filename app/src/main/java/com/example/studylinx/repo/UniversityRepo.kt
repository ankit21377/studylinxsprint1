package com.example.studylinx.repo

import com.example.studylinx.model.University
import kotlinx.coroutines.flow.Flow

interface UniversityRepo {
    fun observeUniversities(): Flow<List<University>>
    suspend fun getUniversityById(id: String): University?
}