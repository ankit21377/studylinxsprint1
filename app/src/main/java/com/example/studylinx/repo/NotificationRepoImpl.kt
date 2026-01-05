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

    override fun observeGlobalNotifications(): Flow<List<NotificationItem>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val items = snap?.documents?.map { doc ->
                    NotificationItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        action = doc.getString("action") ?: "",
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()

                trySend(items).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override fun observeNotificationsForUser(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        val listener = collection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val items = snap?.documents?.map { doc ->
                    NotificationItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        action = doc.getString("action") ?: "",
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()

                trySend(items).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String) {
        collection.document(notificationId).update("isRead", true).await()
    }

    override suspend fun markAllAsReadForUser(userId: String) {
        val snap = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()

        val batch = db.batch()
        for (doc in snap.documents) batch.update(doc.reference, "isRead", true)
        batch.commit().await()
    }

    override suspend fun deleteNotification(notificationId: String) {
        collection.document(notificationId).delete().await()
    }
}
