package  com.healthtech.doccareplusadmin.data.local.datasource.interfaces

import com.healthtech.doccareplusadmin.domain.model.TimePeriod
import com.healthtech.doccareplusadmin.domain.model.TimeSlot
import kotlinx.coroutines.flow.Flow

interface TimeSlotLocalDataSource {
    fun getAllTimeSlots(): Flow<List<TimeSlot>>
    
    fun getTimeSlotsByPeriod(period: TimePeriod): Flow<List<TimeSlot>>

    suspend fun saveTimeSlots(timeSlots: List<TimeSlot>)

    suspend fun deleteAllTimeSlots()

    suspend fun deleteTimeSlotsByPeriod(period: TimePeriod)
}