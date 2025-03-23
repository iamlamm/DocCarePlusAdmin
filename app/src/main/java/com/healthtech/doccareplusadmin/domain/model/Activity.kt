package com.healthtech.doccareplusadmin.domain.model

data class Activity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val type: String = ""
)