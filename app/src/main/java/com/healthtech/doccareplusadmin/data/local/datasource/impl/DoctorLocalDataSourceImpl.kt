package  com.healthtech.doccareplusadmin.data.local.datasource.impl

import com.healthtech.doccareplusadmin.data.local.dao.DoctorDao
import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.DoctorLocalDataSource
import com.healthtech.doccareplusadmin.data.local.entity.DoctorEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DoctorLocalDataSourceImpl @Inject constructor(
    private val doctorDao: DoctorDao
) : DoctorLocalDataSource {
    override fun getDoctors(): Flow<List<DoctorEntity>> = doctorDao.getAllDoctors()

    override suspend fun insertDoctors(doctors: List<DoctorEntity>) {
        doctorDao.insertDoctors(doctors)
    }

    override suspend fun insertDoctor(doctor: DoctorEntity) {
        doctorDao.insertDoctor(doctor)
    }

    override suspend fun updateDoctor(doctor: DoctorEntity) {
        doctorDao.updateDoctor(doctor)
    }

    override suspend fun deleteDoctor(doctorId: Int) {
        doctorDao.deleteDoctor(doctorId)
    }

    override suspend fun deleteAllDoctors() {
        doctorDao.deleteAllDoctors()
    }

    override suspend fun getDoctorById(doctorId: Int): DoctorEntity? = 
        doctorDao.getDoctorById(doctorId)
}