package com.healthtech.doccareplusadmin.domain.model

data class RevenueStats(
    val monthlyRevenue: Map<String, Double> = emptyMap()
)