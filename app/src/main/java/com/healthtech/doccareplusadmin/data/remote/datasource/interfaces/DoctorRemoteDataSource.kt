package com.healthtech.doccareplusadmin.data.remote.datasource.interfaces

import com.healthtech.doccareplusadmin.domain.model.Doctor
import kotlinx.coroutines.flow.Flow

interface DoctorRemoteDataSource {
    fun getDoctors(): Flow<List<Doctor>>
    suspend fun addDoctor(doctor: Doctor): Result<Unit>
    suspend fun updateDoctor(doctor: Doctor): Result<Unit>
    suspend fun deleteDoctor(doctorId: Int): Result<Unit>
    suspend fun getDoctorById(doctorId: Int): Result<Doctor>
}