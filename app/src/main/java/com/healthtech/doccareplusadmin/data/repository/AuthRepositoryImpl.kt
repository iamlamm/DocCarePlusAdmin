package  com.healthtech.doccareplusadmin.data.repository

import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import com.healthtech.doccareplusadmin.data.remote.api.AuthApi
import com.healthtech.doccareplusadmin.domain.model.Admin
import com.healthtech.doccareplusadmin.domain.repository.AuthRepository
import com.zegocloud.zimkit.services.ZIMKit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val adminPreferences: AdminPreferences
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Result<Admin> {
        return try {
            val result = authApi.login(email, password)
            if (result.isSuccess) {
                result.getOrNull()?.let { admin ->
                    if (rememberMe) {
                        adminPreferences.saveAdmin(admin)
                    } else {
                        adminPreferences.clearAdmin()
                    }
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun logout() {
        ZIMKit.disconnectUser()
        adminPreferences.clearAdmin()
        authApi.signOut()
    }
}