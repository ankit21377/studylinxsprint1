package com.example.studylinx.repo

import com.example.studylinx.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepo {
    fun streamAppointmentsInRange(
        userId: String,
        startMillisInclusive: Long,
        endMillisExclusive: Long
    ): Flow<List<Appointment>>

    suspend fun addAppointment(userId: String, appointment: Appointment): String
}