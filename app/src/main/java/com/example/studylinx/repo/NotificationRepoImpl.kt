// File: com/example/studylinx/repo/NotificationRepoImpl.kt
package com.example.studylinx.repo

import com.example.studylinx.model.NotificationItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NotificationRepo {

    private fun globalRef() = db.collection("notifications")
    private fun userRef(userId: String) = db.collection("users").document(userId).collection("notifications")

    override fun observeGlobal(): Flow<List<NotificationItem>> = callbackFlow {
        val reg = globalRef()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { d ->
                    NotificationItem(
                        id = d.id,
                        targetUserId = d.getString("targetUserId") ?: "",
                        userName = d.getString("userName") ?: "",
                        title = d.getString("title") ?: "",
                        message = d.getString("message") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L,
                        isRead = d.getBoolean("isRead") ?: false   // ✅
                    )
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    override fun observeForUser(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val reg = userRef(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { d ->
                    NotificationItem(
                        id = d.id,
                        targetUserId = userId,
                        userName = d.getString("userName") ?: "",
                        title = d.getString("title") ?: "",
                        message = d.getString("message") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L,
                        isRead = d.getBoolean("isRead") ?: false  // ✅
                    )
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    override suspend fun markAsRead(userId: String, notificationId: String) {
        if (userId.isBlank() || notificationId.isBlank()) return
        userRef(userId).document(notificationId)
            .set(mapOf("isRead" to true), SetOptions.merge())
            .await()
    }

    override suspend fun delete(userId: String, notificationId: String) {
        if (userId.isBlank() || notificationId.isBlank()) return
        userRef(userId).document(notificationId).delete().await()
    }

    override suspend fun createNotification(
        targetUserId: String,
        userName: String,
        title: String,
        message: String
    ) {
        val now = System.currentTimeMillis()

        val data = mapOf(
            "targetUserId" to targetUserId.trim(),
            "userName" to userName.trim(),
            "title" to title.trim(),
            "message" to message.trim(),
            "createdAt" to now,
            "isRead" to false  // ✅
        )

        if (targetUserId.isNotBlank()) {
            userRef(targetUserId).add(data).await()
        } else {
            globalRef().add(data).await()
        }
    }
}
