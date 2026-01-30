// File: com/example/studylinx/repo/HomeRepo.kt
package com.example.studylinx.repo

import com.example.studylinx.model.*
import kotlinx.coroutines.flow.Flow

interface HomeRepo {
    fun observeCountries(): Flow<List<Country>>
    fun observeUniversities(limit: Int = 10): Flow<List<University>>
    fun observeUpcomingAppointment(): Flow<Appointment?>
    fun observeProgress(): Flow<ApplicationProgress>

    suspend fun updateProgress(stepIndex: Int, completed: Boolean)
    suspend fun seedIfEmpty()
}
