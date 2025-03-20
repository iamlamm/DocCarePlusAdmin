package  com.healthtech.doccareplusadmin.data.repository

import android.util.Log
import com.healthtech.doccareplusadmin.data.local.datasource.impl.DoctorLocalDataSourceImpl
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.data.remote.datasource.impl.DoctorRemoteDataSourceImpl
import com.healthtech.doccareplusadmin.domain.repository.DoctorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import toDoctor
import toDoctorEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException


@Singleton
class DoctorRepositoryImpl @Inject constructor(
    private val remoteDataSource: DoctorRemoteDataSourceImpl,
    private val localDataSource: DoctorLocalDataSourceImpl
) : DoctorRepository {
//    override fun observeDoctors(): Flow<Result<List<Doctor>>> = channelFlow {
//        launch {
//            try {
//                localDataSource.getDoctors()
//                    .catch { e ->
//                        if (e !is CancellationException) {
//                            send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
//                        }
//                    }
//                    .map { listEntities -> listEntities.map { it.toDoctor() } }
//                    .collect { localDoctors ->
//                        if (localDoctors.isNotEmpty()) {
//                            send(Result.success(localDoctors))
//                        }
//                    }
//            } catch (e: Exception) {
//                send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
//                Log.e("DoctorRepository", "Error loading local data", e)
//            }
//        }
//
//        launch {
//            try {
//                var lastLocalDoctors: List<Doctor>? = null
//
//                // Keep track of local changes
//                localDataSource.getDoctors()
//                    .map { it.map { entity -> entity.toDoctor() } }
//                    .collect { localDoctors ->
//                        lastLocalDoctors = localDoctors
//                    }
//
//                // Listen to remote changes
//                remoteDataSource.getDoctors()
//                    .catch { e ->
//                        if (e !is CancellationException) {
//                            send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
//                        }
//                    }
//                    .collect { remoteDoctors ->
//                        // Compare with local data
//                        if (lastLocalDoctors != remoteDoctors) {
//                            // Update local cache if different
//                            localDataSource.insertDoctors(remoteDoctors.map { it.toDoctorEntity() })
//                            // Emit new data
//                            send(Result.success(remoteDoctors))
//                        }
//                    }
//            } catch (e: Exception) {
//                send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
//                Log.e("DoctorRepository", "Error fetching remote data", e)
//            }
//        }
//    }

    override fun observeDoctors(): Flow<Result<List<Doctor>>> = callbackFlow {
        try {
            localDataSource.getDoctors()
                .catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                    }
                }
                .map { listEntities -> listEntities.map { it.toDoctor() } }
                .collect { localDoctors ->
                    if (localDoctors.isNotEmpty()) {
                        send(Result.success(localDoctors))
                    }

                    // 2. After emitting local data, fetch remote data
                    try {
                        remoteDataSource.getDoctors()
                            .catch { e ->
                                if (e !is CancellationException) {
                                    send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                                }
                            }
                            .collect { remoteDoctors ->
                                // Compare with local data
                                if (localDoctors != remoteDoctors) {
                                    // Update local cache if different
                                    localDataSource.insertDoctors(remoteDoctors.map { it.toDoctorEntity() })
                                    // Emit new data
                                    send(Result.success(remoteDoctors))
                                }
                            }
                    } catch (e: Exception) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                        Log.e("DoctorRepository", "Error fetching remote data", e)
                    }
                }
        } catch (e: Exception) {
            send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
            Log.e("DoctorRepository", "Error loading local data", e)
        }
    }

    override suspend fun addDoctor(doctor: Doctor): Result<Unit> {
        return try {
            // Thêm vào remote trước
            remoteDataSource.addDoctor(doctor).onSuccess {
                // Nếu thành công thì thêm vào local
                localDataSource.insertDoctor(doctor.toDoctorEntity())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi thêm bác sĩ: ${e.message}"))
        }
    }

    override suspend fun updateDoctor(doctor: Doctor): Result<Unit> {
        return try {
            // Cập nhật remote trước
            remoteDataSource.updateDoctor(doctor).onSuccess {
                // Nếu thành công thì cập nhật local
                localDataSource.updateDoctor(doctor.toDoctorEntity())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi cập nhật bác sĩ: ${e.message}"))
        }
    }

    override suspend fun deleteDoctor(doctorId: String): Result<Unit> {
        return try {
            // Xóa từ remote trước
            remoteDataSource.deleteDoctor(doctorId).onSuccess {
                // Nếu thành công thì xóa từ local
                localDataSource.deleteDoctor(doctorId)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi xóa bác sĩ: ${e.message}"))
        }
    }

    override suspend fun getDoctorById(doctorId: String): Result<Doctor?> {
        return try {
            // Thử lấy từ local trước
            localDataSource.getDoctorById(doctorId)?.let {
                return Result.success(it.toDoctor())
            }

            // Nếu không có trong local, lấy từ remote
            remoteDataSource.getDoctorById(doctorId)
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi lấy thông tin bác sĩ: ${e.message}"))
        }
    }

    // Thêm phương thức lọc bác sĩ theo category
    override fun getDoctorsByCategory(categoryId: Int): Flow<Result<List<Doctor>>> = channelFlow {
        launch {
            try {
                // Đầu tiên emit dữ liệu local đã được lọc
                localDataSource.getDoctors().catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                    }
                }.map { listEntities ->
                    // Lọc theo categoryId và chuyển đổi thành Doctor
                    listEntities.filter { it.categoryId == categoryId }
                        .map { it.toDoctor() }
                }.collect { filteredDoctors ->
                    if (filteredDoctors.isNotEmpty()) {
                        send(Result.success(filteredDoctors))
                    }
                }
            } catch (e: Exception) {
                send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                Log.e("DoctorRepository", "Error loading local data by category", e)
            }
        }

        launch {
            // Remote
            try {
                remoteDataSource.getDoctors().catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                    }
                }.map { doctors ->
                    // Lọc remote data theo categoryId
                    doctors.filter { it.categoryId == categoryId }
                }.collect { filteredDoctors ->
                    // Không cần lưu lại vào local database vì đã được lưu đầy đủ bởi observeDoctors()
                    send(Result.success(filteredDoctors))
                }
            } catch (e: Exception) {
                send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                Log.e("DoctorRepository", "Error fetching remote data by category", e)
            }
        }
    }
}