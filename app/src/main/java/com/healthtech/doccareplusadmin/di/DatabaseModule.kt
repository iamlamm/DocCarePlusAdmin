package  com.healthtech.doccareplusadmin.di

import android.content.Context
import androidx.room.Room
import com.healthtech.doccareplusadmin.data.local.AppDataBase
import com.healthtech.doccareplusadmin.data.local.dao.CategoryDao
import com.healthtech.doccareplusadmin.data.local.dao.DoctorDao
import com.healthtech.doccareplusadmin.data.local.dao.TimeSlotDao
import com.healthtech.doccareplusadmin.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDataBase {
        return Room.databaseBuilder(context, AppDataBase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDataBase): CategoryDao {
        return database.categoryDao()
    }


    @Provides
    @Singleton
    fun provideDoctorDao(database: AppDataBase): DoctorDao {
        return database.doctorDao()
    }

    @Provides
    @Singleton
    fun provideTimeSlotDao(database: AppDataBase): TimeSlotDao {
        return database.timeSlotDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDataBase): UserDao {
        return database.userDao()
    }
}