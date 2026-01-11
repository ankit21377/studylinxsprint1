package com.example.studylinx.repo

import com.example.studylinx.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepo {
    fun observeGlobalNotifications(): Flow<List<NotificationItem>>
    fun observeNotificationsForUser(userId: String): Flow<List<NotificationItem>>

    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsReadForUser(userId: String)
    suspend fun deleteNotification(notificationId: String)
}