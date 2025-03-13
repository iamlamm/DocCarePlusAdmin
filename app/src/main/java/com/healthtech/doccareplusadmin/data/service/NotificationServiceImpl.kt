package  com.healthtech.doccareplusadmin.data.service

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Notification
import com.healthtech.doccareplusadmin.domain.service.NotificationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationServiceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : NotificationService {
    override suspend fun createNotification(notification: Notification) {
        val notificationsRef = database.getReference("notifications")
        val newKey = notificationsRef.push().key!!

        notificationsRef.child(newKey).setValue(notification.copy(id = newKey)).await()
    }

    override fun observeNotifications(userId: String): Flow<Result<List<Notification>>> =
        callbackFlow {
            val notificationsRef =
                database.getReference("notifications").orderByChild("userId").equalTo(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = snapshot.children.mapNotNull {
                        it.getValue(Notification::class.java)
                    }.sortedByDescending { it.time }
                    trySend(Result.success(notifications))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Result.failure(error.toException()))
                }

            }
            notificationsRef.addValueEventListener(listener)
            awaitClose { notificationsRef.removeEventListener(listener) }
        }

    override suspend fun markAsRead(notificationId: String) {
        database.getReference("notifications")
            .child(notificationId)
            .child("read")
            .setValue(true)
            .await()
    }
}