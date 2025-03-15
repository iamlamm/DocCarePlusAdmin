package  com.healthtech.doccareplusadmin.data.remote.datasource.impl

import com.healthtech.doccareplusadmin.data.remote.api.FirebaseApi
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.DoctorRemoteDataSource
import com.healthtech.doccareplusadmin.domain.model.Doctor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DoctorRemoteDataSourceImpl @Inject constructor(
    private val firebaseApi: FirebaseApi
) : DoctorRemoteDataSource {
    override fun getDoctors(): Flow<List<Doctor>> = firebaseApi.getDoctors()
    
    override suspend fun addDoctor(doctor: Doctor): Result<Unit> = 
        firebaseApi.addDoctor(doctor)

    override suspend fun updateDoctor(doctor: Doctor): Result<Unit> = 
        firebaseApi.updateDoctor(doctor)

    override suspend fun deleteDoctor(doctorId: String): Result<Unit> =
        firebaseApi.deleteDoctor(doctorId)

    override suspend fun getDoctorById(doctorId: String): Result<Doctor> =
        firebaseApi.getDoctorById(doctorId)
}