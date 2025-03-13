package  com.healthtech.doccareplusadmin.domain.model

data class TimeSlot(
    val id: Int,
    val startTime: String,
    val endTime: String,
    val period: TimePeriod
)