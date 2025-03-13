package  com.healthtech.doccareplusadmin.domain.repository

import com.healthtech.doccareplusadmin.domain.model.Admin

interface AuthRepository {
    suspend fun login(email: String, password: String, rememberMe: Boolean): Result<Admin>

    fun logout()
}