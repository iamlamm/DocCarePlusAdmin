package  com.healthtech.doccareplusadmin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey
    val id: Int,
    val code: String,
    val name: String,
    val specialty: String,
    val categoryId: Int,
    val rating: Double,
    val reviews: Int,
    val fee: Double,
    val image: String,
    val available: Boolean,
    val biography: String,
    val email: String,
    val phoneNumber: String,
    val emergencyContact: String,
    val address: String
)
