package  com.healthtech.doccareplusadmin.utils

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class NetworkUtils @Inject constructor(
    private val connectivityManager: ConnectivityManager
) {
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                )
    }
}