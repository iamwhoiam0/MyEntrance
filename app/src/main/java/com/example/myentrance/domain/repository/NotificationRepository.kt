package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.Notification

interface NotificationRepository {
    suspend fun getUserNotifications(userId: String): List<Notification>
    suspend fun markAsRead(notificationId: String): Boolean
    suspend fun sendNotification(notification: Notification): Boolean
}
