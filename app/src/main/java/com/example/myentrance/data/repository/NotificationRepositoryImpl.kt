package com.example.myentrance.data.repository

import com.example.myentrance.domain.entities.Notification
import com.example.myentrance.domain.entities.NotificationType
import com.example.myentrance.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NotificationRepository {

    override suspend fun getUserNotifications(userId: String): List<Notification> {
        val snap = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .get().await()
        return snap.documents.mapNotNull { it.toNotification() }
    }

    override suspend fun markAsRead(notificationId: String): Boolean {
        firestore.collection("notifications").document(notificationId)
            .update("isRead", true).await()
        return true
    }

    override suspend fun sendNotification(notification: Notification): Boolean {
        firestore.collection("notifications")
            .document(notification.id)
            .set(notification.toMap())
            .await()
        return true
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toNotification(): Notification? {
        return try {
            Notification(
                id = id,
                userId = getString("userId") ?: "",
                type = NotificationType.valueOf(getString("type") ?: NotificationType.SYSTEM.name),
                title = getString("title") ?: "",
                message = getString("message") ?: "",
                isRead = getBoolean("isRead") ?: false,
                createdAt = getLong("createdAt") ?: 0
            )
        } catch (e: Exception) { null }
    }

    private fun Notification.toMap() = mapOf(
        "userId" to userId,
        "type" to type.name,
        "title" to title,
        "message" to message,
        "isRead" to isRead,
        "createdAt" to createdAt
    )
}