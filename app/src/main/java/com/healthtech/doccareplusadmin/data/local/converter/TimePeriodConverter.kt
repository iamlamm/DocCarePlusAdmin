package  com.healthtech.doccareplusadmin.data.local.converter

import androidx.room.TypeConverter
import com.healthtech.doccareplusadmin.domain.model.TimePeriod

class TimePeriodConverter {
    @TypeConverter
    fun fromTimePeriod(period: TimePeriod): String {
        return period.name
    }

    @TypeConverter
    fun toTimePeriod(period: String): TimePeriod {
        return TimePeriod.valueOf(period)
    }
}