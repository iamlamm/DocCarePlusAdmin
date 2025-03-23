package com.healthtech.doccareplusadmin.data.remote.api

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Activity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityApi @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val activitiesRef = database.getReference("activities")

    fun getAllActivities(): Flow<List<Activity>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activityList = mutableListOf<Activity>()
                for (activitySnapshot in snapshot.children) {
                    activitySnapshot.getValue(Activity::class.java)?.let { activity ->
                        activityList.add(activity)
                    }
                }
                activityList.sortByDescending { it.timestamp }
                trySend(activityList)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error getAllActivities: ${error.message}")
                trySend(emptyList())
            }
        }
        activitiesRef.addValueEventListener(listener)
        awaitClose { activitiesRef.removeEventListener(listener) }
    }

    fun getRecentActivities(limit: Int): Flow<List<Activity>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val activityList = mutableListOf<Activity>()
                    for (activitySnapshot in snapshot.children) {
                        val activity = Activity(
                            id = activitySnapshot.key ?: "",
                            title = activitySnapshot.child("title").getValue(String::class.java) ?: "",
                            description = activitySnapshot.child("description").getValue(String::class.java) ?: "",
                            timestamp = activitySnapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
                            type = activitySnapshot.child("type").getValue(String::class.java) ?: "unknown"
                        )
                        activityList.add(activity)
                    }
                    activityList.sortByDescending { it.timestamp }
                    Timber.d("Loaded ${activityList.size} activities")
                    trySend(activityList.take(limit))
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing activities")
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error getRecentActivities: ${error.message}")
                trySend(emptyList())
            }
        }
        
        activitiesRef.orderByChild("timestamp").limitToLast(limit).addValueEventListener(listener)
        awaitClose {
            activitiesRef.orderByChild("timestamp").limitToLast(limit).removeEventListener(listener)
        }
    }

    suspend fun addActivity(activity: Activity): Boolean {
        return try {
            val activityWithId = if (activity.id.isBlank()) {
                activity.copy(id = UUID.randomUUID().toString())
            } else {
                activity
            }
            activitiesRef.child(activityWithId.id).setValue(activityWithId).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi thêm activity")
            false
        }
    }

    suspend fun deleteActivity(activityId: String): Boolean {
        return try {
            activitiesRef.child(activityId).removeValue().await()
            Timber.d("Đã xóa activity: $activityId")
            true
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa activity: $activityId")
            false
        }
    }
}