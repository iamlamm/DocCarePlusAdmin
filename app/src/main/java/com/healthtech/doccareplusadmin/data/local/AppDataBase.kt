package com.healthtech.doccareplusadmin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthtech.doccareplusadmin.data.local.converter.TimePeriodConverter
import com.healthtech.doccareplusadmin.data.local.dao.CategoryDao
import com.healthtech.doccareplusadmin.data.local.dao.DoctorDao
import com.healthtech.doccareplusadmin.data.local.dao.TimeSlotDao
import com.healthtech.doccareplusadmin.data.local.dao.UserDao
import com.healthtech.doccareplusadmin.data.local.entity.CategoryEntity
import com.healthtech.doccareplusadmin.data.local.entity.DoctorEntity
import com.healthtech.doccareplusadmin.data.local.entity.TimeSlotEntity
import com.healthtech.doccareplusadmin.data.local.entity.UserEntity

@Database(
    entities = [CategoryEntity::class, DoctorEntity::class, UserEntity::class, TimeSlotEntity::class],
    version = 1
)
@TypeConverters(TimePeriodConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun doctorDao(): DoctorDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun userDao(): UserDao
}