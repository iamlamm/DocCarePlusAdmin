package  com.healthtech.doccareplusadmin.data.remote.datasource.impl

import com.google.firebase.database.FirebaseDatabase
import com.healthtech.doccareplusadmin.data.remote.api.FirebaseApi
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.TimeSlotRemoteDataSource
import com.healthtech.doccareplusadmin.domain.model.TimePeriod
import com.healthtech.doccareplusadmin.domain.model.TimeSlot
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TimeSlotRemoteDataSourceImpl @Inject constructor(
    private val firebaseApi: FirebaseApi
) : TimeSlotRemoteDataSource {
    override fun getAllTimeSlots(): Flow<List<TimeSlot>> {
        return firebaseApi.getTimeSlots()
    }
}