package com.healthtech.doccareplusadmin.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import com.healthtech.doccareplusadmin.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val adminPreferences: AdminPreferences
) : ViewModel() {

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminRole = MutableStateFlow<UserRole?>(null)
    val adminRole: StateFlow<UserRole?> = _adminRole.asStateFlow()

    init {
        checkLoginStatus()
        loadAdminRole()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isAdminLoggedIn.value = adminPreferences.isAdminLoggedIn()
        }
    }

    private fun loadAdminRole() {
        viewModelScope.launch {
            val admin = adminPreferences.getAdmin()
            _adminRole.value = admin?.role
        }
    }

    fun hasPermission(permission: String): Boolean {
        val admin = adminPreferences.getAdmin() ?: return false
        
        // Kiểm tra trong map permissions trước
        if (admin.permissions.containsKey(permission)) {
            return admin.permissions[permission] == true
        }
        
        // Fallback: Nếu không có trong map, kiểm tra theo role
        return when (permission) {
            "MANAGE_CATEGORIES" -> admin.role == UserRole.ADMIN
            "MANAGE_DOCTORS" -> admin.role == UserRole.ADMIN
            "MANAGE_USERS" -> admin.role == UserRole.ADMIN
            "VIEW_REPORTS" -> admin.role == UserRole.ADMIN
            else -> false
        }
    }
}