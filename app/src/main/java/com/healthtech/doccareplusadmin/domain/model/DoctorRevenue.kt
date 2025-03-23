package com.healthtech.doccareplusadmin.domain.model

data class DoctorRevenue(
    val totalAppointments: Int = 0,
    val monthlyRevenue: Map<String, Double> = emptyMap()
)