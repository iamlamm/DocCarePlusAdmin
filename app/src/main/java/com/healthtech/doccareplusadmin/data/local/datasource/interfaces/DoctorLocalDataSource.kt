package  com.healthtech.doccareplusadmin.data.local.datasource.interfaces

import com.healthtech.doccareplusadmin.data.local.entity.DoctorEntity
import kotlinx.coroutines.flow.Flow

interface DoctorLocalDataSource {
    fun getDoctors(): Flow<List<DoctorEntity>>

    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    suspend fun insertDoctor(doctor: DoctorEntity)

    suspend fun updateDoctor(doctor: DoctorEntity)

    suspend fun deleteDoctor(doctorId: String)

    suspend fun deleteAllDoctors()

    suspend fun getDoctorById(doctorId: String): DoctorEntity?
}