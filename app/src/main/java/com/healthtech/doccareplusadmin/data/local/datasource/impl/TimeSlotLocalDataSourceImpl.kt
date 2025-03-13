package  com.healthtech.doccareplusadmin.data.local.datasource.impl

import com.healthtech.doccareplusadmin.data.local.dao.TimeSlotDao
import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.TimeSlotLocalDataSource
import com.healthtech.doccareplusadmin.domain.mapper.toTimeSlot
import com.healthtech.doccareplusadmin.domain.mapper.toTimeSlotEntity
import com.healthtech.doccareplusadmin.domain.model.TimePeriod
import com.healthtech.doccareplusadmin.domain.model.TimeSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TimeSlotLocalDataSourceImpl @Inject constructor(
    private val timeSlotDao: TimeSlotDao
) : TimeSlotLocalDataSource {
    override fun getAllTimeSlots(): Flow<List<TimeSlot>> {
        return timeSlotDao.getAllTimeSlots()
            .map { entities -> entities.map { it.toTimeSlot() } }
    }

    override fun getTimeSlotsByPeriod(period: TimePeriod): Flow<List<TimeSlot>> {
        return timeSlotDao.getTimeSlotsByPeriod(period)
            .map { entities -> entities.map { it.toTimeSlot() } }
    }

    override suspend fun saveTimeSlots(timeSlots: List<TimeSlot>) {
        timeSlotDao.insertTimeSlots(timeSlots.map { it.toTimeSlotEntity() })
    }

    override suspend fun deleteAllTimeSlots() {
        timeSlotDao.deleteAllTimeSlots()
    }

    override suspend fun deleteTimeSlotsByPeriod(period: TimePeriod) {
        timeSlotDao.deleteTimeSlotsByPeriod(period)
    }

}