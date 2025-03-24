package com.healthtech.doccareplusadmin.data.remote.api

import android.annotation.SuppressLint
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.model.AppointmentStats
import com.healthtech.doccareplusadmin.domain.model.OverviewStats
import com.healthtech.doccareplusadmin.domain.model.RevenueStats
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

@Singleton
class ReportApi @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val adminStatsRef = database.getReference("adminStats")

    fun getOverviewStats(): Flow<OverviewStats> = callbackFlow {
        try {
            // Lấy số lượng bác sĩ
            val doctorsRef = database.getReference("doctors")
            val usersRef = database.getReference("users")
            val appointmentsRef = database.getReference("appointments/details")

            var doctorsCount = 0
            var patientsCount = 0
            var totalAppointments = 0
            var pendingAppointments = 0
            var completedAppointments = 0
            var cancelledAppointments = 0
            var totalRevenue = 0.0
            var dataLoaded = 0

            // Hàm kiểm tra và gửi dữ liệu khi đã load xong
            fun checkAndSendStats() {
                if (dataLoaded == 3) {
                    val stats = OverviewStats(
                        totalDoctors = doctorsCount,
                        totalPatients = patientsCount,
                        totalAppointments = totalAppointments,
                        pendingAppointments = pendingAppointments,
                        completedAppointments = completedAppointments,
                        cancelledAppointments = cancelledAppointments,
                        totalRevenue = totalRevenue,
                        appointmentCompletionRate = if (totalAppointments > 0) 
                            (completedAppointments.toFloat() / totalAppointments) * 100 
                        else 0f
                    )

                    Timber.d("""
                        Overview Stats:
                        - Doctors: $doctorsCount
                        - Patients: $patientsCount
                        - Total Appointments: $totalAppointments
                        - Pending: $pendingAppointments
                        - Completed: $completedAppointments
                        - Cancelled: $cancelledAppointments
                        - Total Revenue: $totalRevenue
                    """.trimIndent())

                    trySend(stats)
                }
            }

            // Lấy số lượng bác sĩ
            doctorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    doctorsCount = snapshot.childrenCount.toInt()
                    dataLoaded++
                    checkAndSendStats()
                }
                override fun onCancelled(error: DatabaseError) {
                    Timber.e("Error getting doctors count: ${error.message}")
                }
            })

            // Lấy số lượng bệnh nhân - sửa lại cách đếm
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    patientsCount = snapshot.children.count { userSnapshot ->
                        userSnapshot.child("role").getValue(String::class.java) == "PATIENT"
                    }
                    dataLoaded++
                    checkAndSendStats()
                }
                override fun onCancelled(error: DatabaseError) {
                    Timber.e("Error getting patients count: ${error.message}")
                }
            })

            // Lấy thông tin cuộc hẹn
            appointmentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { appointment ->
                        totalAppointments++
                        when (appointment.child("status").getValue(String::class.java)) {
                            "upcoming" -> pendingAppointments++
                            "completed" -> completedAppointments++
                            "cancelled" -> cancelledAppointments++
                        }
                        val fee = appointment.child("fee").getValue(Double::class.java) ?: 0.0
                        totalRevenue += fee
                    }
                    dataLoaded++
                    checkAndSendStats()
                }
                override fun onCancelled(error: DatabaseError) {
                    Timber.e("Error getting appointments data: ${error.message}")
                }
            })

        } catch (e: Exception) {
            Timber.e(e, "Error getting overview stats")
            trySend(OverviewStats())
        }

        awaitClose { }
    }

    fun getRevenueStats(): Flow<RevenueStats> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val monthlyRevenue = mutableMapOf<String, Double>()
                    
                    snapshot.children.forEach { appointmentSnapshot ->
                        val fee = appointmentSnapshot.child("fee").getValue(Double::class.java) ?: 0.0
                        val date = appointmentSnapshot.child("date").getValue(String::class.java) ?: ""
                        
                        if (date.isNotEmpty()) {
                            val month = date.substringBeforeLast("-")
                            monthlyRevenue[month] = (monthlyRevenue[month] ?: 0.0) + fee
                        }
                    }

                    Timber.d("Monthly revenue data: $monthlyRevenue")
                    trySend(RevenueStats(monthlyRevenue = monthlyRevenue))
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing revenue stats")
                    trySend(RevenueStats())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error getting revenue stats: ${error.message}")
                trySend(RevenueStats())
            }
        }

        database.getReference("appointments/details").addValueEventListener(listener)
        awaitClose { database.getReference("appointments/details").removeEventListener(listener) }
    }

    fun getAppointmentStats(): Flow<AppointmentStats> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val appointmentsByDate = mutableMapOf<String, Int>()
                    
                    snapshot.children.forEach { dateSnapshot ->
                        val date = dateSnapshot.key ?: return@forEach
                        appointmentsByDate[date] = dateSnapshot.childrenCount.toInt()
                    }

                    Timber.d("Parsed appointments by date: $appointmentsByDate")
                    trySend(AppointmentStats(appointmentsByDate = appointmentsByDate))
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing appointment stats")
                    trySend(AppointmentStats())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error getting appointment stats: ${error.message}")
                trySend(AppointmentStats())
            }
        }

        database.getReference("appointments/byDate").addValueEventListener(listener)
        awaitClose { database.getReference("appointments/byDate").removeEventListener(listener) }
    }

    @SuppressLint("RestrictedApi")
    suspend fun getActivitiesForReport(startDate: Long, endDate: Long): Result<List<Activity>> =
        try {
            Timber.d("Fetching activities from $startDate to $endDate")
            val activitiesRef = database.getReference("activities")
            
            // Log query parameters
            Timber.d("""
                Query parameters:
                Start date: ${Date(startDate)}
                End date: ${Date(endDate)}
                Reference path: ${activitiesRef.path}
            """.trimIndent())

            val snapshot = activitiesRef
                .orderByChild("timestamp")
                .startAt(startDate.toDouble())
                .endAt(endDate.toDouble())
                .get()
                .await()

            Timber.d("Raw activities data: ${snapshot.value}")

            val activityList = mutableListOf<Activity>()
            for (activitySnapshot in snapshot.children) {
                try {
                    val activity = Activity(
                        id = activitySnapshot.key ?: "",
                        title = activitySnapshot.child("title").getValue(String::class.java) ?: "",
                        description = activitySnapshot.child("description").getValue(String::class.java) ?: "",
                        timestamp = activitySnapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
                        type = activitySnapshot.child("type").getValue(String::class.java) ?: "unknown"
                    )
                    activityList.add(activity)
                    Timber.d("Added activity: $activity")
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing activity: ${activitySnapshot.key}")
                }
            }

            activityList.sortByDescending { it.timestamp }
            Timber.d("Total activities found: ${activityList.size}")

            if (activityList.isEmpty()) {
                Timber.w("No activities found in date range: $startDate to $endDate")
            }

            Result.success(activityList)
        } catch (e: Exception) {
            Timber.e(e, "Error getting activities for report: ${e.message}")
            Result.failure(e)
        }
}