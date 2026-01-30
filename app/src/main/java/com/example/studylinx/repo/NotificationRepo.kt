package com.example.studylinx.repo

import com.example.studylinx.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepo {
    fun observeForUserAndGlobal(userId: String): Flow<List<NotificationItem>>
    suspend fun markAsRead(notificationId: String)
    suspend fun deleteNotification(notificationId: String)

    // Admin
    suspend fun createNotification(
        targetUserId: String,   // "ALL" or specific uid
        userName: String,
        action: String,
        details: String,
        likeCount: Int = 0
    )
}