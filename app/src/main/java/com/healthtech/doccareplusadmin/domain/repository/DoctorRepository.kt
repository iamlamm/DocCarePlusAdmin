package  com.healthtech.doccareplusadmin.domain.repository

import com.healthtech.doccareplusadmin.domain.model.Doctor
import kotlinx.coroutines.flow.Flow

interface DoctorRepository {
    fun observeDoctors(): Flow<Result<List<Doctor>>>

    fun getDoctorsByCategory(categoryId: Int): Flow<Result<List<Doctor>>>

    suspend fun addDoctor(doctor: Doctor): Result<Unit>
    suspend fun updateDoctor(doctor: Doctor): Result<Unit>
    suspend fun deleteDoctor(doctorId: String): Result<Unit>
    suspend fun getDoctorById(doctorId: String): Result<Doctor?>
}