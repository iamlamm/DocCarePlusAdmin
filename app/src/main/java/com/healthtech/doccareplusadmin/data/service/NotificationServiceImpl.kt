package com.healthtech.doccareplusadmin.data.service

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Notification
import com.healthtech.doccareplusadmin.domain.model.NotificationType
import com.healthtech.doccareplusadmin.domain.service.NotificationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationServiceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : NotificationService {

    override suspend fun createAdminNotification(notification: Notification) {
        try {
            val notificationsRef = database.getReference("adminNotifications")
            val newKey = notificationsRef.push().key!!

            notificationsRef.child(newKey)
                .setValue(notification.copy(id = newKey))
                .await()
            
            Timber.d("Created admin notification with id: $newKey")
        } catch (e: Exception) {
            Timber.e(e, "Error creating admin notification")
            throw e
        }
    }

    override fun observeAdminNotifications(adminId: String): Flow<Result<List<Notification>>> = callbackFlow {
        val notificationsRef = database.getReference("notifications/admin")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val notifications = snapshot.children.mapNotNull { 
                        try {
                            // Parse từng field riêng lẻ để tránh lỗi enum
                            val id = it.key ?: return@mapNotNull null
                            val title = it.child("title").getValue(String::class.java) ?: ""
                            val message = it.child("message").getValue(String::class.java) ?: ""
                            val time = it.child("time").getValue(Long::class.java) ?: System.currentTimeMillis()
                            val read = it.child("read").getValue(Boolean::class.java) ?: false
                            
                            // Xử lý type an toàn hơn
                            val typeStr = it.child("type").getValue(String::class.java)
                            val type = try {
                                NotificationType.valueOf(typeStr ?: "SYSTEM")
                            } catch (e: IllegalArgumentException) {
                                Timber.w("Unknown notification type: $typeStr, fallback to SYSTEM")
                                NotificationType.SYSTEM
                            }

                            Notification(
                                id = id,
                                title = title,
                                message = message,
                                time = time,
                                type = type,
                                read = read
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "Error parsing notification")
                            null
                        }
                    }.sortedByDescending { it.time }
                    
                    Timber.d("Loaded ${notifications.size} admin notifications")
                    trySend(Result.success(notifications))
                } catch (e: Exception) {
                    Timber.e(e, "Error processing notifications")
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Database error")
                trySend(Result.failure(error.toException()))
            }
        }

        notificationsRef.addValueEventListener(listener)
        awaitClose { 
            notificationsRef.removeEventListener(listener) 
        }
    }

    override suspend fun markAdminNotificationAsRead(notificationId: String, adminId: String) {
        try {
            database.getReference("notifications/admin")
                .child(notificationId)
                .child("read")
                .setValue(true)
                .await()
            
            Timber.d("Marked admin notification as read: $notificationId")
        } catch (e: Exception) {
            Timber.e(e, "Error marking notification as read")
            throw e
        }
    }

    override fun getUnreadNotificationsCount(adminId: String): Flow<Result<Int>> = callbackFlow {
        val notificationsRef = database.getReference("notifications/admin")
            .orderByChild("read")
            .equalTo(false)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val count = snapshot.childrenCount.toInt()
                    Timber.d("Unread admin notifications count: $count")
                    trySend(Result.success(count))
                } catch (e: Exception) {
                    Timber.e(e, "Error getting unread count")
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Database error")
                trySend(Result.failure(error.toException()))
            }
        }

        notificationsRef.addValueEventListener(listener)
        awaitClose { 
            notificationsRef.removeEventListener(listener) 
        }
    }
}