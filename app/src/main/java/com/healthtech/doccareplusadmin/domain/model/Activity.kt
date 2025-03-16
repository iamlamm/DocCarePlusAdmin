package com.healthtech.doccareplusadmin.domain.model

data class Activity(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val type: String // appointment, user_registration, doctor_approval, etc.
)