package com.example.studylinx.repo

import kotlinx.coroutines.flow.Flow

interface CourseIndexRepo {
    fun observeUniversityIdsForCourse(courseKey: String): Flow<List<String>>

    suspend fun addUniversityToCourse(courseKey: String, uniId: String)
    suspend fun removeUniversityFromCourse(courseKey: String, uniId: String)

    suspend fun deleteCourseIndex(courseKey: String) // remove whole index node
}
