package  com.healthtech.doccareplusadmin.data.remote.datasource.impl

import com.healthtech.doccareplusadmin.data.remote.api.DoctorApi
import com.healthtech.doccareplusadmin.data.remote.api.FirebaseApi
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.DoctorRemoteDataSource
import com.healthtech.doccareplusadmin.domain.model.Doctor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DoctorRemoteDataSourceImpl @Inject constructor(
    private val doctorApi: DoctorApi
) : DoctorRemoteDataSource {
    override fun getDoctors(): Flow<List<Doctor>> = doctorApi.getDoctors()
    
    override suspend fun addDoctor(doctor: Doctor): Result<Unit> =
        doctorApi.addDoctor(doctor)

    override suspend fun updateDoctor(doctor: Doctor): Result<Unit> =
        doctorApi.updateDoctor(doctor)

    override suspend fun deleteDoctor(doctorId: String): Result<Unit> =
        doctorApi.deleteDoctor(doctorId)

    override suspend fun getDoctorById(doctorId: String): Result<Doctor?> =
        doctorApi.getDoctorById(doctorId)
}