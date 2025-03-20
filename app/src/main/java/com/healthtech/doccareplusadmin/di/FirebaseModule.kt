package  com.healthtech.doccareplusadmin.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.BuildConfig
import com.google.firebase.database.FirebaseDatabase
import com.healthtech.doccareplusadmin.data.remote.api.AuthApi
import com.healthtech.doccareplusadmin.data.remote.api.CategoryApi
import com.healthtech.doccareplusadmin.data.remote.api.DashboardApi
import com.healthtech.doccareplusadmin.data.remote.api.DoctorApi
import com.healthtech.doccareplusadmin.data.remote.api.FirebaseApi
import com.healthtech.doccareplusadmin.data.remote.api.UserApi
import com.healthtech.doccareplusadmin.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseApi(database: FirebaseDatabase, auth: FirebaseAuth): FirebaseApi {
        return FirebaseApi(database, auth)
    }

    @Provides
    @Singleton
    fun provideDashboardApi(database: FirebaseDatabase): DashboardApi {
        return DashboardApi(database)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        if (BuildConfig.DEBUG) {
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        }
        return auth
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        auth: FirebaseAuth, database: FirebaseDatabase, networkUtils: NetworkUtils
    ): AuthApi {
        return AuthApi(auth, database, networkUtils)
    }

    @Provides
    @Singleton
    fun provideCategoryApi(database: FirebaseDatabase): CategoryApi {
        return CategoryApi(database)
    }

    @Provides
    @Singleton
    fun provideDoctorApi(database: FirebaseDatabase): DoctorApi {
        return DoctorApi(database)
    }

    @Provides
    @Singleton
    fun provideUserApi(database: FirebaseDatabase, auth: FirebaseAuth): UserApi {
        return UserApi(database, auth)
    }
}