package  com.healthtech.doccareplusadmin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.healthtech.doccareplusadmin.data.local.entity.TimeSlotEntity
import com.healthtech.doccareplusadmin.domain.model.TimePeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {
    @Query("SELECT * FROM time_slots")
    fun getAllTimeSlots(): Flow<List<TimeSlotEntity>>

    @Query("SELECT * FROM time_slots WHERE period = :period")
    fun getTimeSlotsByPeriod(period: TimePeriod): Flow<List<TimeSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlots(timeSlots: List<TimeSlotEntity>)

    @Query("DELETE FROM time_slots")
    suspend fun deleteAllTimeSlots()

    // Thêm phương thức này nếu cần
    @Query("DELETE FROM time_slots WHERE period = :period")
    suspend fun deleteTimeSlotsByPeriod(period: TimePeriod)
}