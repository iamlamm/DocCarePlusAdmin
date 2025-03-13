package  com.healthtech.doccareplusadmin.di

import android.content.Context
import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): AdminPreferences {
        return AdminPreferences(context)
    }
}