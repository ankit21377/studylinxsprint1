// File: com/example/studylinx/model/NotificationItem.kt
package com.example.studylinx.model

data class NotificationItem(
    val id: String = "",
    val targetUserId: String = "",     // optional
    val userName: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false        // ✅ MATCHES YOUR UI
)
