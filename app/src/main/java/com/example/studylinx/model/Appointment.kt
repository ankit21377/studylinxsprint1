package com.example.studylinx.model

data class Appointment(
    val id: String = "",
    val counselorName: String = "",
    val status: String = "Pending",
    val dateTimeMillis: Long = 0L
)
