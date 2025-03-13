package  com.healthtech.doccareplusadmin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthtech.doccareplusadmin.data.local.entity.DoctorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors")
    fun getAllDoctors(): Flow<List<DoctorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: DoctorEntity)

    @Update
    suspend fun updateDoctor(doctor: DoctorEntity)

    @Query("DELETE FROM doctors WHERE id = :doctorId")
    suspend fun deleteDoctor(doctorId: Int)

    @Query("DELETE FROM doctors")
    suspend fun deleteAllDoctors()

    @Query("SELECT * FROM doctors WHERE id = :doctorId")
    suspend fun getDoctorById(doctorId: Int): DoctorEntity?
}