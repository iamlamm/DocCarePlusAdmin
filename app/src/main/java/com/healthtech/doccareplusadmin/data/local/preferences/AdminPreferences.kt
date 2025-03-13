package com.healthtech.doccareplusadmin.data.local.preferences

import android.content.Context
import com.healthtech.doccareplusadmin.domain.model.Admin
import com.healthtech.doccareplusadmin.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ADMIN_ID = "admin_id"
        private const val KEY_ADMIN_NAME = "admin_name"
        private const val KEY_ADMIN_EMAIL = "admin_email"
        private const val KEY_ADMIN_ROLE = "admin_role"
        private const val KEY_ADMIN_AVATAR = "admin_avatar"
        private const val KEY_ADMIN_CREATED_AT = "admin_created_at"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REMEMBER_ME = "remember_me"
    }

    fun saveAdmin(admin: Admin) {
        sharedPreferences.edit().apply {
            putString(KEY_ADMIN_ID, admin.id)
            putString(KEY_ADMIN_NAME, admin.name)
            putString(KEY_ADMIN_EMAIL, admin.email)
            putString(KEY_ADMIN_ROLE, admin.role.name)
            putString(KEY_ADMIN_AVATAR, admin.avatar)
            putLong(KEY_ADMIN_CREATED_AT, admin.createdAt)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getAdmin(): Admin? {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)

        if (!isLoggedIn || !rememberMe) return null

        return Admin(
            id = sharedPreferences.getString(KEY_ADMIN_ID, "") ?: "",
            name = sharedPreferences.getString(KEY_ADMIN_NAME, "") ?: "",
            email = sharedPreferences.getString(KEY_ADMIN_EMAIL, "") ?: "",
            role = UserRole.valueOf(
                sharedPreferences.getString(KEY_ADMIN_ROLE, UserRole.ADMIN.name)
                    ?: UserRole.ADMIN.name
            ),
            avatar = sharedPreferences.getString(KEY_ADMIN_AVATAR, null),
            createdAt = sharedPreferences.getLong(KEY_ADMIN_CREATED_AT, System.currentTimeMillis())
        )
    }

    fun clearAdmin() {
        sharedPreferences.edit().apply {
            remove(KEY_ADMIN_ID)
            remove(KEY_ADMIN_NAME)
            remove(KEY_ADMIN_EMAIL)
            remove(KEY_ADMIN_ROLE)
            remove(KEY_ADMIN_AVATAR)
            remove(KEY_ADMIN_CREATED_AT)
            remove(KEY_IS_LOGGED_IN)
            apply()
        }
    }

    fun saveRememberMe(isChecked: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_REMEMBER_ME, isChecked).apply()
    }

    fun isRememberMeChecked(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun isAdminLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
                sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }
}