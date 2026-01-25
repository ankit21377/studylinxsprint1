package com.example.studylinx.model

data class University(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val country: String = "",
    val countryId: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val locationUrl: String = "",
    val courses: List<String> = emptyList()
)
