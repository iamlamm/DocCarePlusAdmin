package com.healthtech.doccareplusadmin.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.domain.repository.ActivityRepository
import com.healthtech.doccareplusadmin.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AllUsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    // All users state
    private val _usersState = MutableStateFlow<UiState<List<User>>>(UiState.Idle)
    val usersState: StateFlow<UiState<List<User>>> = _usersState.asStateFlow()

    // Filtered users state
    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers.asStateFlow()

    // Delete state
    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Flag for tracking if search is active
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Flag for tracking if refresh is needed
    private val _needsRefresh = MutableStateFlow(false)

    // Lưu trữ danh sách gốc để tìm kiếm
    private var originalUsers = listOf<User>()

    // Theo dõi thời gian cập nhật cuối
    private var lastRefreshTime = 0L

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _usersState.value = UiState.Loading
            try {
                userRepository.observeUsers()
                    .collect { result ->
                        result.onSuccess { users ->
                            Timber.d("Received ${users.size} users from repository")
                            val uniqueUsers = users.distinctBy { it.id }

                            if (uniqueUsers.size != users.size) {
                                Timber.w("Detected duplicates in repository data: ${users.size} -> ${uniqueUsers.size} after filtering")
                                // Log duplicate IDs
                                val duplicateIds = users.groupBy { it.id }
                                    .filter { it.value.size > 1 }
                                    .keys
                                Timber.w("Duplicate user IDs: $duplicateIds")
                            }

                            originalUsers = uniqueUsers
                            _usersState.value = UiState.Success(uniqueUsers)

                            filterUsers(_searchQuery.value)

                            lastRefreshTime = System.currentTimeMillis()
                            _needsRefresh.value = false
                        }.onFailure { error ->
                            Timber.e(error, "Failed to fetch users")
                            _usersState.value =
                                UiState.Error(error.message ?: "Failed to fetch users")
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching users")
                _usersState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterUsers(query)
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            _filteredUsers.value = originalUsers
        }
    }

    private fun filterUsers(query: String) {
        if (query.isEmpty()) {
            _filteredUsers.value = originalUsers
            return
        }

        val lowerCaseQuery = query.lowercase()
        _filteredUsers.value = originalUsers.filter { user ->
            user.name.lowercase().contains(lowerCaseQuery) ||
                    user.email.lowercase().contains(lowerCaseQuery) ||
                    user.phoneNumber.lowercase().contains(lowerCaseQuery)
        }
    }

    fun forceRefresh() {
        fetchUsers()
    }

    fun checkAndRefreshIfNeeded() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime > 60000 || _needsRefresh.value) {
            forceRefresh()
        }
    }

    fun setNeedsRefresh() {
        _needsRefresh.value = true
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            try {
                val user = userRepository.getUserById(userId).getOrNull()
                userRepository.deleteUser(userId).onSuccess {
                    _deleteState.value = UiState.Success(Unit)
                    user?.let {
                        val activity = Activity(
                            id = "",
                            title = "Xóa người dùng",
                            description = "Người dùng ${it.name} đã bị xóa khỏi hệ thống",
                            timestamp = System.currentTimeMillis(),
                            type = "user_deleted"
                        )
                        activityRepository.addActivity(activity)
                    }
                }.onFailure { error ->
                    _deleteState.value = UiState.Error(error.message ?: "Failed to delete user")
                }
            } catch (e: Exception) {
                _deleteState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = UiState.Idle
    }
}