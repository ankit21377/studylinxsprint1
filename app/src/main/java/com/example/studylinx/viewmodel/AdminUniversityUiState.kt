package com.example.studylinx.viewmodel

data class AdminUniversityUiState(
    val name: String = "",
    val city: String = "",
    val country: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val locationUrl: String = "",
    val saving: Boolean = false,
    val message: String? = null
)
