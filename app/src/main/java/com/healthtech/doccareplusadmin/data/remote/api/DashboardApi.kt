package com.healthtech.doccareplusadmin.data.remote.api

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.model.AppointmentsStats
import com.healthtech.doccareplusadmin.domain.model.DoctorRevenue
import com.healthtech.doccareplusadmin.domain.model.MonthlyRevenue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardApi @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val adminStatsRef = database.getReference("adminStats")
    private val TAG = "DashboardApi"

    suspend fun getDoctorsCount(): Result<Int> = try {
        val snapshot = database.getReference("doctors")
            .get()
            .await()
        Result.success(snapshot.childrenCount.toInt())
    } catch (e: Exception) {
        Timber.e(e, "Error getting doctors count")
        Result.failure(e)
    }

    suspend fun getUsersCount(): Result<Int> = try {
        val snapshot = database.getReference("users")
            .get()
            .await()
        Result.success(snapshot.childrenCount.toInt())
    } catch (e: Exception) {
        Timber.e(e, "Error getting users count")
        Result.failure(e)
    }

    suspend fun getTodayAppointmentsCount(): Result<Int> = try {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())

        val snapshot = database.getReference("adminStats/appointmentsByDate/$today")
            .get()
            .await()

        val count = snapshot.getValue(Int::class.java) ?: 0
        Timber.d("Today's appointments count from adminStats: $count")
        Result.success(count)
    } catch (e: Exception) {
        Timber.e(e, "Error getting today's appointments")
        Result.failure(e)
    }

    suspend fun getMonthlyRevenue(): Result<Double> = try {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            .format(Date())

        val snapshot = database.getReference("adminStats/revenueByMonth/$currentMonth")
            .get()
            .await()

        val amount = snapshot.child("totalAmount").getValue(Double::class.java) ?: 0.0
        Timber.d("Monthly revenue from adminStats: $amount")
        Result.success(amount)
    } catch (e: Exception) {
        Timber.e(e, "Error calculating monthly revenue")
        Result.failure(e)
    }

    suspend fun getUpcomingAppointmentsCount(): Result<Int> = try {
        val snapshot = database.getReference("adminStats/appointmentsStatus/upcoming")
            .get()
            .await()

        val count = snapshot.getValue(Int::class.java) ?: 0
        Timber.d("Upcoming appointments count: $count")
        Result.success(count)
    } catch (e: Exception) {
        Timber.e(e, "Error getting upcoming appointments count")
        Result.failure(e)
    }

    suspend fun getUnreadNotificationsCount(): Result<Int> = try {
        val snapshot = database.getReference("adminStats/unreadNotifications")
            .get()
            .await()

        val count = snapshot.getValue(Int::class.java) ?: 0
        Timber.d("Unread notifications count: $count")
        Result.success(count)
    } catch (e: Exception) {
        Timber.e(e, "Error getting unread notifications count")
        Result.failure(e)
    }

    suspend fun getRecentActivities(): Result<List<Activity>> = try {
        val snapshot = database.getReference("activities")
            .orderByChild("timestamp")
            .limitToLast(10)
            .get()
            .await()

        val activities = snapshot.children.mapNotNull { activitySnapshot ->
            try {
                Activity(
                    id = activitySnapshot.key ?: return@mapNotNull null,
                    title = activitySnapshot.child("title").getValue(String::class.java) ?: "",
                    description = activitySnapshot.child("description").getValue(String::class.java)
                        ?: "",
                    timestamp = activitySnapshot.child("timestamp").getValue(Long::class.java)
                        ?: 0L,
                    type = activitySnapshot.child("type").getValue(String::class.java) ?: "unknown"
                )
            } catch (e: Exception) {
                Timber.e(e, "Error parsing activity")
                null
            }
        }.sortedByDescending { it.timestamp }

        Result.success(activities)
    } catch (e: Exception) {
        Timber.e(e, "Error getting recent activities")
        Result.failure(e)
    }

    fun getAppointmentsStats(): Flow<AppointmentsStats> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val stats = AppointmentsStats(
                        appointmentsByDate = snapshot.child("appointmentsByDate")
                            .children
                            .associate { 
                                it.key.toString() to (it.value as Long).toInt() 
                            },
                        
                        appointmentsStatus = snapshot.child("appointmentsStatus")
                            .children
                            .associate { 
                                it.key.toString() to (it.value as Long).toInt() 
                            },
                        
                        revenueByDoctor = snapshot.child("revenueByDoctor")
                            .children
                            .associate { doctorSnapshot ->
                                doctorSnapshot.key.toString() to DoctorRevenue(
                                    totalAppointments = (doctorSnapshot.child("totalAppointments").value as Long).toInt(),
                                    monthlyRevenue = doctorSnapshot.children
                                        .filter { it.key != "totalAppointments" }
                                        .associate { 
                                            it.key.toString() to (it.value as Double) 
                                        }
                                )
                            },
                        
                        revenueByMonth = snapshot.child("revenueByMonth")
                            .children
                            .associate { monthSnapshot ->
                                monthSnapshot.key.toString() to MonthlyRevenue(
                                    appointmentsCount = (monthSnapshot.child("appointmentsCount").value as Long).toInt(),
                                    totalAmount = monthSnapshot.child("totalAmount").value as Double
                                )
                            }
                    )
                    
                    Timber.d("Appointments stats loaded: $stats")
                    trySend(stats)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing appointments stats")
                    trySend(AppointmentsStats())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error getting appointments stats: ${error.message}")
                trySend(AppointmentsStats())
            }
        }

        adminStatsRef.addValueEventListener(listener)
        awaitClose { adminStatsRef.removeEventListener(listener) }
    }
}