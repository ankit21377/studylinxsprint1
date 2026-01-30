package com.example.studylinx.repo

import com.example.studylinx.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepo {

    fun observeGlobal(): Flow<List<NotificationItem>>                 // ALL users
    fun observeForUser(userId: String): Flow<List<NotificationItem>>  // ALL + specific user

    suspend fun markAsRead(notificationId: String)
    suspend fun delete(notificationId: String)

    suspend fun createNotification(
        targetUserId: String,  // "ALL" or UID
        userName: String,
        title: String,
        message: String,
        likeCount: Int = 0
    )
}