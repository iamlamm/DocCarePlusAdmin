package com.healthtech.doccareplusadmin.domain.model

data class OverviewStats(
    val totalDoctors: Int = 0,
    val totalPatients: Int = 0,
    val totalAppointments: Int = 0,
    val pendingAppointments: Int = 0,
    val completedAppointments: Int = 0,
    val cancelledAppointments: Int = 0,
    val totalRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val appointmentCompletionRate: Float = 0f,
    val averageRevenuePerAppointment: Double = 0.0
)