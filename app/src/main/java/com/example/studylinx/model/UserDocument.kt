package com.example.studylinx.model

data class UserDocument(
    val docType: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val storagePath: String = "",
    val uploadedAt: Long = 0L,
    val updatedAt: Long = 0L
)
