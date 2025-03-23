package com.healthtech.doccareplusadmin.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.data.local.preferences.AdminPreferences
import com.healthtech.doccareplusadmin.data.remote.api.DashboardApi
import com.healthtech.doccareplusadmin.domain.model.AppointmentsStats
import com.healthtech.doccareplusadmin.domain.repository.ActivityRepository
import com.healthtech.doccareplusadmin.domain.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val adminPreferences: AdminPreferences,
    private val notificationService: NotificationService,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _dashboardState = MutableLiveData(DashboardState())
    val dashboardState: LiveData<DashboardState> = _dashboardState

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    private val _appointmentsStats = MutableStateFlow<AppointmentsStats?>(null)
    val appointmentsStats: StateFlow<AppointmentsStats?> = _appointmentsStats.asStateFlow()

    init {
        setCurrentDate()
        loadDashboardData()
        observeUnreadNotifications()
        loadRecentActivities()
    }

    private fun loadRecentActivities(limit: Int = 10) {
        viewModelScope.launch {
            try {
                _dashboardState.value = _dashboardState.value?.copy(isLoading = true)
                
                activityRepository.getRecentActivities(limit)
                    .catch { e -> 
                        Timber.e(e, "Error loading recent activities")
                        emit(emptyList())
                    }
                    .collect { activities ->
                        Timber.d("Received ${activities.size} activities")
                        _dashboardState.value = _dashboardState.value?.copy(
                            recentActivities = activities,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading recent activities")
                _dashboardState.value = _dashboardState.value?.copy(
                    isLoading = false,
                    error = "Lỗi tải hoạt động gần đây: ${e.message}"
                )
            }
        }
    }

    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi"))
        _currentDate.value = dateFormat.format(Date())
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _dashboardState.value = _dashboardState.value?.copy(isLoading = true, error = null)

                // Parallel loading using coroutines
                val doctorsDeferred = async { dashboardApi.getDoctorsCount() }
                val usersDeferred = async { dashboardApi.getUsersCount() }
                val appointmentsDeferred = async { dashboardApi.getTodayAppointmentsCount() }
                val upcomingDeferred = async { dashboardApi.getUpcomingAppointmentsCount() }
                val revenueDeferred = async { dashboardApi.getMonthlyRevenue() }
                val notificationsDeferred = async { dashboardApi.getUnreadNotificationsCount() }

                // Collect all results
                val doctors = doctorsDeferred.await()
                val users = usersDeferred.await()
                val appointments = appointmentsDeferred.await()
                val upcoming = upcomingDeferred.await()
                val revenue = revenueDeferred.await()
                val notifications = notificationsDeferred.await()

                // Update state with all data
                _dashboardState.value = _dashboardState.value?.copy(
                    isLoading = false,
                    doctorsCount = doctors.getOrDefault(0),
                    usersCount = users.getOrDefault(0),
                    todayAppointments = appointments.getOrDefault(0),
                    upcomingAppointments = upcoming.getOrDefault(0),
                    monthlyRevenue = revenue.getOrDefault(0.0),
                    unreadNotifications = notifications.getOrDefault(0)
                )

                // Load appointments stats
                dashboardApi.getAppointmentsStats().collect { stats ->
                    _dashboardState.value = _dashboardState.value?.copy(
                        appointmentsStats = stats
                    )
                }

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
        loadRecentActivities()
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