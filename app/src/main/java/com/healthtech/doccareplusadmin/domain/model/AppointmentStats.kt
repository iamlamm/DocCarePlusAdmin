package com.healthtech.doccareplusadmin.domain.model

data class AppointmentStats(
    val appointmentsByDate: Map<String, Int> = emptyMap()
)