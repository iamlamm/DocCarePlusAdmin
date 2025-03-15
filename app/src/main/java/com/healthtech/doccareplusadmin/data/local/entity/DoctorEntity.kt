package  com.healthtech.doccareplusadmin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val name: String,
    val specialty: String,
    val categoryId: Int,
    val rating: Float,
    val reviews: Long,
    val fee: Double,
    val avatar: String,
    val available: Boolean,
    val biography: String,
    val role: String,
    val email: String,
    val phoneNumber: String,
    val emergencyContact: String,
    val address: String
)
