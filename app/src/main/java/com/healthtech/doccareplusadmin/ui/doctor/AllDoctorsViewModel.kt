package com.healthtech.doccareplusadmin.ui.doctor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.domain.repository.DoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

@HiltViewModel
class AllDoctorsViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Chỉ để theo dõi trạng thái đã tải xong chưa
    private val _isInitialDataLoaded = MutableStateFlow(false)
    val isInitialDataLoaded = _isInitialDataLoaded.asStateFlow()

    // Lưu trữ thông tin category
    private val _categoryId = MutableStateFlow<Int?>(null)
    val categoryId = _categoryId.asStateFlow()

    private val _categoryName = MutableStateFlow<String?>(null)
    val categoryName = _categoryName.asStateFlow()

    // Theo dõi thời gian cập nhật cuối
    private var lastRefreshTime = 0L

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Doctor>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Lưu trữ danh sách gốc
    private var originalDoctors = listOf<Doctor>()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    // Trạng thái doctors
    private val _doctors = MutableStateFlow<UiState<List<Doctor>>>(UiState.Loading)
    val doctors: StateFlow<UiState<List<Doctor>>> = _doctors.asStateFlow()

    // Trạng thái xóa doctor
    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState = _deleteState.asStateFlow()

    init {
        // Lấy categoryId và categoryName từ navigation arguments
        savedStateHandle.get<Int>("categoryId")?.let { catId ->
            if (catId > -1) { // Kiểm tra giá trị mặc định -1
                _categoryId.value = catId
            }
        }

        savedStateHandle.get<String>("categoryName")?.let { catName ->
            _categoryName.value = catName
        }

        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val catId = _categoryId.value

            if (catId != null) {
                // Nếu có categoryId, load doctors theo category
                loadDoctorsByCategory(catId)
            } else {
                // Nếu không có categoryId, load tất cả doctors
                fetchAllDoctors()
            }

            _isInitialDataLoaded.value = true
        }
    }

    private fun loadDoctorsByCategory(categoryId: Int) {
        viewModelScope.launch {
            try {
                _doctors.value = UiState.Loading

                doctorRepository.getDoctorsByCategory(categoryId).collect { result ->
                    result.onSuccess { doctors ->
                        originalDoctors = doctors
                        _doctors.value = UiState.Success(doctors)
                        if (!_isSearchActive.value) {
                            _searchResults.value = doctors
                        } else {
                            filterDoctors(_searchQuery.value)
                        }
                    }.onFailure { error ->
                        _doctors.value = UiState.Error(error.message ?: "Failed to load doctors by category")
                        Timber.e(error, "Error loading doctors by category")
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _doctors.value = UiState.Error(e.message ?: "Unknown error")
                    Timber.e(e, "Exception loading doctors by category")
                }
            }
        }
    }

    private fun fetchAllDoctors() {
        viewModelScope.launch {
            try {
                _doctors.value = UiState.Loading
                
                doctorRepository.observeDoctors().collect { result ->
                    result.onSuccess { doctorsList ->
                        _doctors.value = UiState.Success(doctorsList)
                        originalDoctors = doctorsList
                        _searchResults.value = doctorsList
                        _isInitialDataLoaded.value = true
                        
                        // Log để debug
                        Timber.d("Loaded ${doctorsList.size} doctors successfully")
                    }.onFailure { error ->
                        _doctors.value = UiState.Error(error.message ?: "Failed to load doctors")
                        Timber.e(error, "Error loading doctors")
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Exception loading doctors")
                _doctors.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Hàm đặt query tìm kiếm
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterDoctors(query)
    }

    // Hàm lọc danh sách doctors
    private fun filterDoctors(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = originalDoctors
            return
        }

        val filteredList = originalDoctors.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.specialty.contains(query, ignoreCase = true)
        }

        _searchResults.value = filteredList
    }

    // Hàm xóa query tìm kiếm
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = originalDoctors
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            clearSearch()
        }
    }

    // Cập nhật phương thức forceRefresh để đảm bảo hiển thị loading state
    fun forceRefresh() {
        viewModelScope.launch {
            lastRefreshTime = System.currentTimeMillis()
            
            // Đặt trạng thái loading để hiển thị loading indicator
            _doctors.value = UiState.Loading
            
            // Đảm bảo các danh sách kết quả được reset
            _searchResults.value = emptyList()
            originalDoctors = emptyList()
            
            val catId = _categoryId.value
            if (catId != null) {
                loadDoctorsByCategory(catId)
            } else {
                fetchAllDoctors()
            }
        }
    }

    // Hàm này kiểm tra và refresh chỉ khi cần thiết
    fun checkAndRefreshIfNeeded() {
        val currentTime = System.currentTimeMillis()
        // Chỉ refresh nếu đã trôi qua ít nhất 1 phút từ lần cuối
        if (currentTime - lastRefreshTime > 60000) {
            forceRefresh()
        }
    }

    // Xóa doctor
    fun deleteDoctor(doctorId: Int) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            try {
                doctorRepository.deleteDoctor(doctorId).onSuccess {
                    _deleteState.value = UiState.Success(Unit)
                    // Không cần gọi forceRefresh vì observeAllDoctors sẽ tự cập nhật
                }.onFailure { error ->
                    _deleteState.value = UiState.Error(error.message ?: "Failed to delete doctor")
                }
            } catch (e: Exception) {
                _deleteState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Thêm hàm này để đảm bảo dữ liệu luôn được load khi quay lại fragment
    fun resetLoadState() {
        _isInitialDataLoaded.value = false
    }

    // Thêm phương thức này để reset trạng thái delete
    fun resetDeleteState() {
        _deleteState.value = UiState.Idle
    }
}