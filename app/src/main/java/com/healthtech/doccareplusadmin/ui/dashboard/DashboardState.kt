package com.healthtech.doccareplusadmin.ui.dashboard

import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.model.AppointmentsStats

data class DashboardState(
    val isLoading: Boolean = true,
    val doctorsCount: Int = 0,
    val usersCount: Int = 0,
    val todayAppointments: Int = 0,
    val upcomingAppointments: Int = 0,
    val monthlyRevenue: Double = 0.0,
    val unreadNotifications: Int = 0,
    val recentActivities: List<Activity> = emptyList(),
    val appointmentsStats: AppointmentsStats? = null,
    val error: String? = null
)