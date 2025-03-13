package  com.healthtech.doccareplusadmin.ui.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import com.zegocloud.zimkit.services.ZIMKit
import dagger.hilt.android.lifecycle.HiltViewModel
import im.zego.zim.enums.ZIMErrorCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val adminPreferences: AdminPreferences,
) : ViewModel() {
    private val _startDestination = MutableStateFlow<Int>(0)
    val startDestination = _startDestination.asStateFlow()

    fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val isLoggedIn = adminPreferences.isAdminLoggedIn()
                val admin = adminPreferences.getAdmin()

                Log.d("SplashViewModel", "isLoggedIn: $isLoggedIn, admin: $admin")

                _startDestination.value = if (isLoggedIn && admin != null) {
                    try {
                        admin.avatar?.let { avatar ->
                            connectToZegoCloud(admin.id, admin.name, avatar)
                        }
                    } catch (e: Exception) {
                        Log.e("SplashViewModel", "Error connecting to ZegoCloud: ${e.message}")
                    }
                    R.id.dashboardFragment
                } else {
                    R.id.loginFragment
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Error checking login status: ${e.message}")
                _startDestination.value = R.id.loginFragment
            }
        }
    }

    private fun connectToZegoCloud(userId: String, userName: String, userAvatar: String) {
        try {
            ZIMKit.connectUser(userId, userName, userAvatar) { error ->
                if (error.code != ZIMErrorCode.SUCCESS) {
                    Log.e("SplashViewModel", "ZIMKit reconnect failed: ${error.message}")
                } else {
                    Log.d("SplashViewModel", "ZIMKit reconnect success with userId: $userId")
                }
            }
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Error in connectToZegoCloud: ${e.message}")
        }
    }
}