package  com.healthtech.doccareplusadmin.di

import com.healthtech.doccareplusadmin.data.local.datasource.impl.CategoryLocalDataSourceImpl
import com.healthtech.doccareplusadmin.data.local.datasource.impl.DoctorLocalDataSourceImpl
import com.healthtech.doccareplusadmin.data.local.datasource.impl.TimeSlotLocalDataSourceImpl
import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.CategoryLocalDataSource
import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.DoctorLocalDataSource
import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.TimeSlotLocalDataSource
import com.healthtech.doccareplusadmin.data.remote.datasource.impl.CategoryRemoteDataSourceImpl
import com.healthtech.doccareplusadmin.data.remote.datasource.impl.DoctorRemoteDataSourceImpl
import com.healthtech.doccareplusadmin.data.remote.datasource.impl.TimeSlotRemoteDataSourceImpl
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.CategoryRemoteDataSource
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.DoctorRemoteDataSource
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.TimeSlotRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindCategoryLocalDataSource(
        impl: CategoryLocalDataSourceImpl
    ): CategoryLocalDataSource

    @Binds
    @Singleton
    abstract fun bindDoctorLocalDataSource(
        impl: DoctorLocalDataSourceImpl
    ): DoctorLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCategoryRemoteDataSource(
        impl: CategoryRemoteDataSourceImpl
    ): CategoryRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindDoctorRemoteDataSource(
        impl: DoctorRemoteDataSourceImpl
    ): DoctorRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindTimeSlotLocalDataSource(
        impl: TimeSlotLocalDataSourceImpl
    ): TimeSlotLocalDataSource

    @Binds
    @Singleton
    abstract fun bindTimeSlotRemoteDataSource(
        impl: TimeSlotRemoteDataSourceImpl
    ): TimeSlotRemoteDataSource
}