package  com.healthtech.doccareplusadmin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.healthtech.doccareplusadmin.domain.model.TimePeriod

@Entity(tableName = "time_slots")
data class TimeSlotEntity(
    @PrimaryKey
    val id: Int,
    val startTime: String,
    val endTime: String,
    val period: TimePeriod
)