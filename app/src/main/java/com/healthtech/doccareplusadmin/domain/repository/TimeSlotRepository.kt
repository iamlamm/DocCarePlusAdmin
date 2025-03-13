package  com.healthtech.doccareplusadmin.domain.repository

import com.healthtech.doccareplusadmin.domain.model.TimeSlot
import kotlinx.coroutines.flow.Flow

interface TimeSlotRepository {
    fun observeTimeSlots(): Flow<Result<List<TimeSlot>>>
}