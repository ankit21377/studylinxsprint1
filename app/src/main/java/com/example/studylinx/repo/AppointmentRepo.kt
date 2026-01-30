// File: com/example/studylinx/repo/AppointmentRepo.kt
package com.example.studylinx.repo

import com.example.studylinx.model.Appointment

interface AppointmentRepo {
    fun observeAppointments(
        userId: String,
        onUpdate: (List<Appointment>) -> Unit,
        onError: (String) -> Unit
    )

    fun addAppointment(
        userId: String,
        appointment: Appointment,
        onDone: (Boolean, String?) -> Unit
    )

    fun stop() // important to remove listener
}
