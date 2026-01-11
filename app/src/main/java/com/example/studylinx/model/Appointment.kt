package com.example.studylinx.model

data class Appointment(
    val id: String = "",
    val title: String = "",
    val note: String = "",
    val startMillis: Long = 0L,
    val endMillis: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)