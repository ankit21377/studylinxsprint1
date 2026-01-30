package com.example.studylinx.repo

import com.example.studylinx.model.NotificationItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NotificationRepo {

    private val collection = db.collection("notifications")

    override fun observeGlobal(): Flow<List<NotificationItem>> = callbackFlow {
        val listener = collection
            .whereEqualTo("userId", "ALL")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val list = snap?.documents?.map { doc ->
                    NotificationItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        likeCount = (doc.getLong("likeCount") ?: 0L).toInt(),
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()
                trySend(list).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override fun observeForUser(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        // ✅ show both ALL + user-specific
        val listener = collection
            .whereIn("userId", listOf("ALL", userId))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val list = snap?.documents?.map { doc ->
                    NotificationItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        likeCount = (doc.getLong("likeCount") ?: 0L).toInt(),
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()
                trySend(list).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String) {
        collection.document(notificationId).update("isRead", true).await()
    }

    override suspend fun delete(notificationId: String) {
        collection.document(notificationId).delete().await()
    }

    override suspend fun createNotification(
        targetUserId: String,
        userName: String,
        title: String,
        message: String,
        likeCount: Int
    ) {
        val data = hashMapOf(
            "userId" to targetUserId.ifBlank { "ALL" },
            "userName" to userName,
            "title" to title,
            "message" to message,
            "likeCount" to likeCount,
            "isRead" to false,
            "createdAt" to System.currentTimeMillis()
        )
        collection.add(data).await()
    }
}