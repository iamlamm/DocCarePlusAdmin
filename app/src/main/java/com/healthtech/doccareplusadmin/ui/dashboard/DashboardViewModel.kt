package com.healthtech.doccareplusadmin.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import com.healthtech.doccareplusadmin.data.remote.api.DashboardApi
import com.healthtech.doccareplusadmin.domain.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val adminPreferences: AdminPreferences,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _dashboardState = MutableLiveData(DashboardState())
    val dashboardState: LiveData<DashboardState> = _dashboardState

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    init {
        setCurrentDate()
        loadDashboardData()
        observeUnreadNotifications()
    }

    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi"))
        _currentDate.value = dateFormat.format(Date())
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _dashboardState.value = _dashboardState.value?.copy(isLoading = true, error = null)

                // Log để debug
                Timber.d("Starting to load dashboard data")

                // Lấy số lượng bác sĩ
                dashboardApi.getDoctorsCount().onSuccess { count ->
                    Timber.d("Doctors count: $count")
                    _dashboardState.value = _dashboardState.value?.copy(doctorsCount = count)
                }

                // Lấy số lượng người dùng
                dashboardApi.getUsersCount().onSuccess { count ->
                    Timber.d("Users count: $count")
                    _dashboardState.value = _dashboardState.value?.copy(usersCount = count)
                }

                // Lấy số lượng cuộc hẹn hôm nay
                dashboardApi.getTodayAppointmentsCount().onSuccess { count ->
                    Timber.d("Today's appointments: $count")
                    _dashboardState.value = _dashboardState.value?.copy(todayAppointments = count)
                }

                // Lấy số lượng cuộc hẹn sắp tới
                dashboardApi.getUpcomingAppointmentsCount().onSuccess { count ->
                    Timber.d("Upcoming appointments: $count")
                    _dashboardState.value = _dashboardState.value?.copy(upcomingAppointments = count)
                }

                // Lấy doanh thu tháng
                dashboardApi.getMonthlyRevenue().onSuccess { revenue ->
                    Timber.d("Monthly revenue: $revenue")
                    _dashboardState.value = _dashboardState.value?.copy(monthlyRevenue = revenue)
                }

                // Lấy số thông báo chưa đọc
                dashboardApi.getUnreadNotificationsCount().onSuccess { count ->
                    Timber.d("Unread notifications: $count")
                    _dashboardState.value = _dashboardState.value?.copy(unreadNotifications = count)
                }

                _dashboardState.value = _dashboardState.value?.copy(isLoading = false)
            } catch (e: Exception) {
                Timber.e(e, "Error loading dashboard data")
                _dashboardState.value = _dashboardState.value?.copy(
                    isLoading = false,
                    error = "Không thể tải dữ liệu: ${e.message}"
                )
            }
        }
    }

    fun refreshData() {
        loadDashboardData()
    }

    private fun observeUnreadNotifications() {
        viewModelScope.launch {
            try {
                val adminId = adminPreferences.getAdmin()?.id ?: return@launch
                notificationService.getUnreadNotificationsCount(adminId).collect { result ->
                    result.onSuccess { count ->
                        _unreadNotificationsCount.value = count
                    }.onFailure { e ->
                        Timber.e(e, "Error getting unread notifications count")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error observing unread notifications")
            }
        }
    }
}