package  com.healthtech.doccareplusadmin.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import com.healthtech.doccareplusadmin.domain.model.Admin
import com.healthtech.doccareplusadmin.domain.model.UserRole
import com.healthtech.doccareplusadmin.domain.repository.AuthRepository
import com.healthtech.doccareplusadmin.utils.ZegoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val adminPreferences: AdminPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _rememberMeState = MutableStateFlow(false)
    val rememberMeState = _rememberMeState.asStateFlow()

    init {
        _rememberMeState.value = adminPreferences.isRememberMeChecked()
    }

    fun login(email: String, password: String, rememberMe: Boolean) {
        if (_loginState.value is LoginState.Loading) {
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                adminPreferences.saveRememberMe(rememberMe)

                val result = authRepository.login(email, password, rememberMe)
                if (result.isSuccess) {
                    val admin = adminPreferences.getAdmin()

                    if (admin != null) {
                        if (admin.role != UserRole.ADMIN) {
                            _loginState.value =
                                LoginState.InvalidRole("Tài khoản không có quyền truy cập vào ứng dụng Admin")
                            return@launch
                        }

                        handleLoginSuccess(admin)
//                        connectToZegoCloud(admin.id, admin.name, admin.avatar!!)

                        try {
                            ZegoUtils.connectUser(admin.id, admin.name, admin.avatar!!)
                            ZegoUtils.initZegoCallService(
                                context.applicationContext as Application,
                                admin.id,
                                admin.name
                            )
                        } catch (e: Exception) {
                            Timber.e("Failed to connect Zego services" + e.message)
                        }

                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error("Không tìm thấy thông tin người dùng")
                    }
                } else {
                    _loginState.value =
                        LoginState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập thất bại")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun updateRememberMe(isChecked: Boolean) {
        adminPreferences.saveRememberMe(isChecked)
        _rememberMeState.value = isChecked
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

//    private fun connectToZegoCloud(userId: String, userName: String, userAvatar: String) {
//        ZIMKit.connectUser(userId, userName, userAvatar) { error ->
//            if (error.code != ZIMErrorCode.SUCCESS) {
//                Log.e("LoginViewModel", "ZIMKit connect failed: ${error.message}")
//            } else {
//                Log.d("LoginViewModel", "ZIMKit connect success with userId: $userId")
//            }
//        }
//    }

    private fun handleLoginSuccess(admin: Admin) {
        viewModelScope.launch {
            adminPreferences.saveAdmin(admin)
        }
    }
}