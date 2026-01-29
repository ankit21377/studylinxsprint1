package com.example.studylinx.model

data class Course(
    val id: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
