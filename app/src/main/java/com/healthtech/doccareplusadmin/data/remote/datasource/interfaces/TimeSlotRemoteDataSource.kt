package  com.healthtech.doccareplusadmin.data.remote.datasource.interfaces

import com.healthtech.doccareplusadmin.domain.model.TimePeriod
import com.healthtech.doccareplusadmin.domain.model.TimeSlot
import kotlinx.coroutines.flow.Flow

interface TimeSlotRemoteDataSource {
    fun getAllTimeSlots(): Flow<List<TimeSlot>>
}