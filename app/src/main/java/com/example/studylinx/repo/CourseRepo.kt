package com.example.studylinx.repo

import com.example.studylinx.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepo {
    fun observeCourses(): Flow<List<Course>>
    suspend fun addCourse(name: String): String
    suspend fun updateCourse(courseId: String, newName: String)
    suspend fun deleteCourse(courseId: String)
}