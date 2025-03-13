package com.healthtech.doccareplusadmin.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Doctor(
    val id: Int = 0,
    val name: String = "",
    val specialty: String = "",
    val categoryId: Int = 0,
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val fee: Double = 0.0,
    val image: String = "",
    val code: String = "",
    val biography: String = "",
    val available: Boolean = true,
    val email: String = "",
    val phoneNumber: String = "",
    val emergencyContact: String = "",
    val address: String = ""
) : Parcelable
