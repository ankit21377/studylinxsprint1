package com.example.studylinx.university.vm

import com.example.studylinx.model.University

data class UniversityUiState(
    val loading: Boolean = true,
    val query: String = "",
    val all: List<University> = emptyList(),
    val filtered: List<University> = emptyList()
)
