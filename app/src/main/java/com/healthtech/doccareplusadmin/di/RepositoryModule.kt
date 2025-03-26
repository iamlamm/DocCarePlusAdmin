package  com.healthtech.doccareplusadmin.di

import com.healthtech.doccareplusadmin.data.repository.ActivityRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.AuthRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.CategoryRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.DoctorRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.StorageRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.TimeSlotRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.UserRepositoryImpl
import com.healthtech.doccareplusadmin.domain.repository.ActivityRepository
import com.healthtech.doccareplusadmin.domain.repository.AuthRepository
import com.healthtech.doccareplusadmin.domain.repository.CategoryRepository
import com.healthtech.doccareplusadmin.domain.repository.DoctorRepository
import com.healthtech.doccareplusadmin.domain.repository.StorageRepository
import com.healthtech.doccareplusadmin.domain.repository.TimeSlotRepository
import com.healthtech.doccareplusadmin.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindDoctorRepository(
        doctorRepositoryImpl: DoctorRepositoryImpl
    ): DoctorRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTimeSlotReposiory(
        timeSlotReposiory: TimeSlotRepositoryImpl
    ): TimeSlotRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        activityRepositoryImpl: ActivityRepositoryImpl
    ): ActivityRepository
}