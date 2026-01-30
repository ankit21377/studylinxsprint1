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

    private val col = db.collection("notifications")

    override fun observeForUserAndGlobal(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        // ✅ Show BOTH: user notifications + global ("ALL")
        val listener = col
            .whereIn("userId", listOf(userId, "ALL"))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { doc ->
                    NotificationItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        action = doc.getString("action") ?: "",
                        likeCount = (doc.getLong("likeCount") ?: 0L).toInt(),
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        timeAgo = "" // computed in VM/UI
                    )
                } ?: emptyList()

                trySend(list).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String) {
        col.document(notificationId).update("isRead", true).await()
    }

    override suspend fun deleteNotification(notificationId: String) {
        col.document(notificationId).delete().await()
    }

    override suspend fun createNotification(
        targetUserId: String,
        userName: String,
        action: String,
        details: String,
        likeCount: Int
    ) {
        val data = hashMapOf(
            "userId" to targetUserId,
            "userName" to userName,
            "action" to action,
            "details" to details,
            "likeCount" to likeCount,
            "isRead" to false,
            "createdAt" to System.currentTimeMillis()
        )
        col.add(data).await()
    }
}