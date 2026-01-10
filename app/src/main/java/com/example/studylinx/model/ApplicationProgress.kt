package com.example.studylinx.model

data class ApplicationProgress(
    val currentStep: Int = 0, // 0..3
    val submitted: Boolean = false,
    val inReview: Boolean = false,
    val interview: Boolean = false,
    val finalDecision: Boolean = false
)
