package com.healthtech.doccareplusadmin.domain.model

data class Admin(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val role: UserRole = UserRole.ADMIN,
    val permissions: Map<String, Boolean> = emptyMap()
)