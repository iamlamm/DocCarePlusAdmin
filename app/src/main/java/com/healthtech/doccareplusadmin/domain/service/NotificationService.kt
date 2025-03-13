package  com.healthtech.doccareplusadmin.domain.service

import com.healthtech.doccareplusadmin.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationService {
    suspend fun createNotification(notification: Notification)

    fun observeNotifications(userId: String): Flow<Result<List<Notification>>>

    suspend fun markAsRead(notificationId: String)
}