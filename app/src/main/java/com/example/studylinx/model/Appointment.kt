// File: com/example/studylinx/model/Appointment.kt
package com.example.studylinx.model

/**
 * ONE model used across:
 * - Home upcoming appointment (Firestore) => counselorName + dateTimeMillis + status
 * - AppointmentActivity calendar booking (RTDB) => title + note + startMillis + endMillis + status
 *
 * All fields have defaults so Firebase can deserialize safely.
 */
data class Appointment(
    val id: String = "",
    val userId: String = "",

    // Home screen (Firestore upcoming)
    val counselorName: String = "",
    val dateTimeMillis: Long = 0L,

    // AppointmentActivity (calendar booking / RTDB)
    val title: String = "",
    val note: String = "",
    val startMillis: Long = 0L,
    val endMillis: Long = 0L,

    // Shared
    val status: String = "Pending",
    val createdAt: Long = 0L
)
