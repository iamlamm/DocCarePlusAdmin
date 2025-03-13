package  com.healthtech.doccareplusadmin.domain.mapper

import com.healthtech.doccareplusadmin.data.local.entity.TimeSlotEntity
import com.healthtech.doccareplusadmin.domain.model.TimeSlot

fun TimeSlotEntity.toTimeSlot(): TimeSlot {
    return TimeSlot(
        id = id,
        startTime = startTime,
        endTime = endTime,
        period = period
    )
}

fun TimeSlot.toTimeSlotEntity(): TimeSlotEntity {
    return TimeSlotEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        period = period
    )
}