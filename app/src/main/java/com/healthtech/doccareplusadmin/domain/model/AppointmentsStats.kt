package com.healthtech.doccareplusadmin.domain.model

data class AppointmentsStats(
    val appointmentsByDate: Map<String, Int> = emptyMap(),
    val appointmentsStatus: Map<String, Int> = emptyMap(),
    val revenueByDoctor: Map<String, DoctorRevenue> = emptyMap(),
    val revenueByMonth: Map<String, MonthlyRevenue> = emptyMap()
)

