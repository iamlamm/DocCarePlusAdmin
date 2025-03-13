package  com.healthtech.doccareplusadmin.data.repository

import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.TimeSlotLocalDataSource
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.TimeSlotRemoteDataSource
import com.healthtech.doccareplusadmin.domain.model.TimePeriod
import com.healthtech.doccareplusadmin.domain.model.TimeSlot
import com.healthtech.doccareplusadmin.domain.repository.TimeSlotRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import android.util.Log

class TimeSlotRepositoryImpl @Inject constructor(
    private val localDataSource: TimeSlotLocalDataSource,
    private val remoteDataSource: TimeSlotRemoteDataSource
) : TimeSlotRepository {

    override fun observeTimeSlots(): Flow<Result<List<TimeSlot>>> = flow {
        try {
            Log.d("Repository", "Starting to observe time slots")
            
            // Emit local data first (nếu có)
            val localSlots = localDataSource.getAllTimeSlots().firstOrNull() ?: emptyList()
            if (localSlots.isNotEmpty()) {
                Log.d("Repository", "Local time slots loaded: ${localSlots.size}")
                emit(Result.success(localSlots))
            }
            
            // Luôn cố gắng lấy remote data
            try {
                val remoteSlots = remoteDataSource.getAllTimeSlots().first()
                Log.d("Repository", "Remote time slots received: ${remoteSlots.size}")
                // Lưu vào local nếu cần
                if (remoteSlots.isNotEmpty()) {
                    localDataSource.saveTimeSlots(remoteSlots)
                }
                emit(Result.success(remoteSlots))
            } catch (e: Exception) {
                Log.e("Repository", "Error loading remote slots: ${e.message}")
                // Nếu đã emit local data thì không cần throw lỗi
                if (localSlots.isEmpty()) {
                    emit(Result.failure(e))
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error in observeTimeSlots: ${e.message}")
            emit(Result.failure(e))
        }
    }
}