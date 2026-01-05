package com.example.studylinx.model

data class HomeSummary(
    val appointmentDateTime: String = "",
    val appointmentStatus: String = "",
    val appointmentCounselor: String = "",
    val ieltsExamType: String = "",
    val ieltsNextTestDate: String = "",
    val ieltsStatus: String = "",
    val applicationCurrentStep: Int = 0,
    val applicationSteps: List<String> = emptyList()
)
