package com.healthtech.doccareplusadmin.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Doctor(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val specialty: String = "",
    val categoryId: Int = 0,
    val rating: Float = 0F,
    val reviews: Long = 0L,
    val fee: Double = 0.0,
    val avatar: String = "",
    val available: Boolean = true,
    val biography: String = "",
    val role: UserRole = UserRole.DOCTOR,
    val email: String = "",
    val phoneNumber: String = "",
    val emergencyContact: String = "",
    val address: String = ""
) : Parcelable
