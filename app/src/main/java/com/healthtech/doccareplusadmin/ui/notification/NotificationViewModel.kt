package com.healthtech.doccareplusadmin.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import com.healthtech.doccareplusadmin.domain.model.Notification
import com.healthtech.doccareplusadmin.domain.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationService: NotificationService,
    private val adminPreferences: AdminPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Notification>>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val adminId = adminPreferences.getAdmin()?.id
                Timber.d("Loading notifications for admin: $adminId")
                
                if (adminId == null) {
                    _uiState.value = UiState.Error("Không tìm thấy thông tin admin")
                    return@launch
                }

                notificationService.observeAdminNotifications(adminId).collect { result ->
                    result.onSuccess { notifications ->
                        Timber.d("Loaded ${notifications.size} notifications")
                        _uiState.value = UiState.Success(notifications)
                    }.onFailure { e ->
                        Timber.e(e, "Error loading notifications")
                        _uiState.value = UiState.Error(e.message ?: "Đã có lỗi xảy ra")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadNotifications")
                _uiState.value = UiState.Error(e.message ?: "Đã có lỗi xảy ra")
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val adminId = adminPreferences.getAdmin()?.id ?: return@launch
                notificationService.markAdminNotificationAsRead(notificationId, adminId)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}