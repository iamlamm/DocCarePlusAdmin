package com.healthtech.doccareplusadmin.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.PATIENT,
    val createdAt: Long = System.currentTimeMillis(),
    val avatar: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val age: Int? = null,
    val bloodType: String? = null,
    val about: String? = null,
    val gender: Gender? = null
)