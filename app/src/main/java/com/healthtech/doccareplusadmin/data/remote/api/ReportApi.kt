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

                Timber.d(
                    """
                    Overview Stats:
                    - Doctors: $doctorsCount
                    - Patients: $patientsCount
                    - Total Appointments: $totalAppointments
                    - Pending: $pendingAppointments
                    - Completed: $completedAppointments
                    - Cancelled: $cancelledAppointments
                    - Total Revenue: $totalRevenue
                """.trimIndent()
                )

                trySend(stats)
            }
        }

        // Lấy số lượng bác sĩ
        val doctorsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                doctorsCount = snapshot.childrenCount.toInt()
                dataLoaded++
                checkAndSendStats()
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        // Lấy số lượng bệnh nhân - sửa lại cách đếm
        val usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                patientsCount = snapshot.children.count { userSnapshot ->
                    userSnapshot.child("role").getValue(String::class.java) == "PATIENT"
                }
                dataLoaded++
                checkAndSendStats()
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        // Lấy thông tin cuộc hẹn
        val appointmentsListener = object : ValueEventListener {
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
                close(error.toException())
            }
        }

        try {
            // Đăng ký các listener
            doctorsRef.addValueEventListener(doctorsListener)
            usersRef.addValueEventListener(usersListener)
            appointmentsRef.addValueEventListener(appointmentsListener)
        } catch (e: Exception) {
            Timber.e(e, "Error registering listeners")
            trySend(OverviewStats())
        }

        // awaitClose phải là câu lệnh cuối cùng và không nằm trong try-catch
        awaitClose {
            try {
                doctorsRef.removeEventListener(doctorsListener)
                usersRef.removeEventListener(usersListener)
                appointmentsRef.removeEventListener(appointmentsListener)
            } catch (e: Exception) {
                Timber.e(e, "Error removing listeners")
            }
        }
    }

    fun getRevenueStats(): Flow<RevenueStats> = callbackFlow {
        val revenueByMonthRef = database.getReference("adminStats/revenueByMonth")
        val revenueByDoctorRef = database.getReference("adminStats/revenueByDoctor")

        var monthlyRevenueData: Map<String, RevenueStats.MonthlyRevenueData>? = null
        var doctorRevenueData: Map<String, RevenueStats.DoctorRevenue>? = null
        var dataLoaded = 0

        // Hàm kiểm tra và gửi dữ liệu khi đã load xong
        fun checkAndSendStats() {
            if (dataLoaded == 2 && monthlyRevenueData != null && doctorRevenueData != null) {
                val stats = RevenueStats(
                    monthlyRevenue = monthlyRevenueData!!,
                    revenueByDoctor = doctorRevenueData!!
                )
                trySend(stats)
            }
        }

        // Tạo và lưu trữ các listener
        val monthlyRevenueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val monthlyRevenue = mutableMapOf<String, RevenueStats.MonthlyRevenueData>()

                    snapshot.children.forEach { monthSnapshot ->
                        val month = monthSnapshot.key ?: return@forEach
                        val appointmentsCount = monthSnapshot.child("appointmentsCount")
                            .getValue(Long::class.java)?.toInt() ?: 0
                        val totalAmount = monthSnapshot.child("totalAmount")
                            .getValue(Double::class.java) ?: 0.0

                        monthlyRevenue[month] = RevenueStats.MonthlyRevenueData(
                            appointmentsCount = appointmentsCount,
                            totalAmount = totalAmount
                        )
                    }

                    monthlyRevenueData = monthlyRevenue
                    dataLoaded++
                    checkAndSendStats()

                    Timber.d("Monthly revenue loaded: $monthlyRevenue")
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing monthly revenue")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val doctorRevenueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val doctorRevenue = mutableMapOf<String, RevenueStats.DoctorRevenue>()

                    snapshot.children.forEach { doctorSnapshot ->
                        val doctorId = doctorSnapshot.key ?: return@forEach
                        val totalAppointments = doctorSnapshot.child("totalAppointments")
                            .getValue(Long::class.java)?.toInt() ?: 0

                        val monthlyRevenue = mutableMapOf<String, Double>()
                        doctorSnapshot.children.forEach { child ->
                            if (child.key != "totalAppointments") {
                                monthlyRevenue[child.key!!] =
                                    child.getValue(Double::class.java) ?: 0.0
                            }
                        }

                        doctorRevenue[doctorId] = RevenueStats.DoctorRevenue(
                            totalAppointments = totalAppointments,
                            monthlyRevenue = monthlyRevenue
                        )
                    }

                    doctorRevenueData = doctorRevenue
                    dataLoaded++
                    checkAndSendStats()

                    Timber.d("Doctor revenue loaded: $doctorRevenue")
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing doctor revenue")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        try {
            // Đăng ký các listener
            revenueByMonthRef.addValueEventListener(monthlyRevenueListener)
            revenueByDoctorRef.addValueEventListener(doctorRevenueListener)
        } catch (e: Exception) {
            Timber.e(e, "Error in getRevenueStats")
            trySend(RevenueStats())
        }

        // awaitClose phải là câu lệnh cuối cùng
        awaitClose {
            try {
                revenueByMonthRef.removeEventListener(monthlyRevenueListener)
                revenueByDoctorRef.removeEventListener(doctorRevenueListener)
            } catch (e: Exception) {
                Timber.e(e, "Error removing listeners")
            }
        }
    }

    // Các hàm tiện ích để xử lý dữ liệu doanh thu
    private fun calculateTotalRevenue(stats: RevenueStats): Double {
        return stats.monthlyRevenue.values.sumOf { it.totalAmount }
    }

    private fun calculateTotalAppointments(stats: RevenueStats): Int {
        return stats.monthlyRevenue.values.sumOf { it.appointmentsCount }
    }

    private fun calculateAverageRevenuePerAppointment(stats: RevenueStats): Double {
        val totalAppointments = calculateTotalAppointments(stats)
        return if (totalAppointments > 0) {
            calculateTotalRevenue(stats) / totalAppointments
        } else 0.0
    }

    private fun formatRevenueForChart(stats: RevenueStats): Map<String, Double> {
        return stats.monthlyRevenue.mapValues { it.value.totalAmount }
    }

    private fun formatDoctorRevenueForChart(stats: RevenueStats): Map<String, Double> {
        return stats.revenueByDoctor.mapValues { entry ->
            entry.value.monthlyRevenue.values.sum()
        }
    }

    fun getAppointmentStats(): Flow<AppointmentStats> = callbackFlow {
        val appointmentsRef = database.getReference("appointments")

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
                close(error.toException())
            }
        }

        try {
            appointmentsRef.addValueEventListener(listener)
        } catch (e: Exception) {
            Timber.e(e, "Error registering appointment listener")
            trySend(AppointmentStats())
        }

        // awaitClose phải là câu lệnh cuối cùng
        awaitClose {
            try {
                appointmentsRef.removeEventListener(listener)
            } catch (e: Exception) {
                Timber.e(e, "Error removing listener")
            }
        }
    }

    @SuppressLint("RestrictedApi")
    suspend fun getActivitiesForReport(startDate: Long, endDate: Long): Result<List<Activity>> =
        try {
            Timber.d("Fetching activities from $startDate to $endDate")
            val activitiesRef = database.getReference("activities")

            // Log query parameters
            Timber.d(
                """
                Query parameters:
                Start date: ${Date(startDate)}
                End date: ${Date(endDate)}
                Reference path: ${activitiesRef.path}
            """.trimIndent()
            )

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
                        description = activitySnapshot.child("description")
                            .getValue(String::class.java) ?: "",
                        timestamp = activitySnapshot.child("timestamp").getValue(Long::class.java)
                            ?: 0L,
                        type = activitySnapshot.child("type").getValue(String::class.java)
                            ?: "unknown"
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


