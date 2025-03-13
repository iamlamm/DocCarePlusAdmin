package  com.healthtech.doccareplusadmin.di

import android.content.Context
import android.net.ConnectivityManager
import com.healthtech.doccareplusadmin.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(connectivityManager: ConnectivityManager): NetworkUtils {
        return NetworkUtils(connectivityManager)
    }
}