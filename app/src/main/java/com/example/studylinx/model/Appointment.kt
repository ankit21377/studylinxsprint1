package com.example.studylinx.model

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val counselorName: String = "",
    val dateTimeMillis: Long = 0L,
    val status: String = "Pending"
)
