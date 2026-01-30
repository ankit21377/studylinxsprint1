// File: com/example/studylinx/repo/NotificationRepo.kt
package com.example.studylinx.repo

import com.example.studylinx.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepo {
    fun observeGlobal(): Flow<List<NotificationItem>>
    fun observeForUser(userId: String): Flow<List<NotificationItem>>

    suspend fun markAsRead(userId: String, notificationId: String)
    suspend fun delete(userId: String, notificationId: String)

    suspend fun createNotification(
        targetUserId: String,
        userName: String,
        title: String,
        message: String
    )
}
