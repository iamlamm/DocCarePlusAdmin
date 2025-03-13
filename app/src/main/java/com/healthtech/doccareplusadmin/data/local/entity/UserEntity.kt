package com.healthtech.doccareplusadmin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.healthtech.doccareplusadmin.domain.model.Gender
import com.healthtech.doccareplusadmin.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val createdAt: Long,
    val avatar: String?,
    val height: Int?,
    val weight: Int?,
    val age: Int?,
    val bloodType: String?,
    val about: String?,
    val gender: String?
)