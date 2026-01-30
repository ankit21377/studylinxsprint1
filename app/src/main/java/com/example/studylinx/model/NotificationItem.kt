package com.example.studylinx.model

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false,
    val userId: String = "",
    val userName: String = ""
)
