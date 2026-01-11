package com.example.studylinx.model

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val action: String = "",
    val timeAgo: String = "",
    val likeCount: Int = 0,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)