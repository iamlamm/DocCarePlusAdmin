package com.healthtech.doccareplusadmin.domain.model

data class RevenueStats(
    val monthlyRevenue: Map<String, MonthlyRevenueData> = emptyMap(),
    val revenueByDoctor: Map<String, DoctorRevenue> = emptyMap()
) {
    data class MonthlyRevenueData(
        val appointmentsCount: Int = 0,
        val totalAmount: Double = 0.0
    )

    data class DoctorRevenue(
        val totalAppointments: Int = 0,
        val monthlyRevenue: Map<String, Double> = emptyMap()
    )
}