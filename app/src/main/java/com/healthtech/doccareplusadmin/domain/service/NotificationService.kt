package com.healthtech.doccareplusadmin.domain.service

import com.healthtech.doccareplusadmin.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationService {
    suspend fun createAdminNotification(notification: Notification)

    fun observeAdminNotifications(adminId: String): Flow<Result<List<Notification>>>

    suspend fun markAdminNotificationAsRead(notificationId: String, adminId: String)
    
    // Thêm hàm để lấy số lượng thông báo chưa đọc
    fun getUnreadNotificationsCount(adminId: String): Flow<Result<Int>>
}