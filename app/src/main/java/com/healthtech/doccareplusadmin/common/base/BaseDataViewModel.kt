package com.healthtech.doccareplusadmin.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.domain.repository.CategoryRepository
import com.healthtech.doccareplusadmin.domain.repository.DoctorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.StateFlow

abstract class BaseDataViewModel : ViewModel() {
    
    @Inject
    protected lateinit var categoryRepository: CategoryRepository
    
    @Inject
    protected lateinit var doctorRepository: DoctorRepository

    // Trạng thái danh sách categories
    abstract val categories: StateFlow<UiState<List<Category>>>

    protected val _doctors = MutableStateFlow<UiState<List<Doctor>>>(UiState.Idle)
    val doctors = _doctors.asStateFlow()

    // Cache data
    private var cachedCategories: List<Category>? = null
    private var cachedDoctors: List<Doctor>? = null

    // For tracking active network calls
    private var categoriesJob: Job? = null
    private var doctorsJob: Job? = null

    /**
     * Observe categories with cache support and controlled subscription management
     * @param forceRefresh Set to true to ignore cache and fetch fresh data
     */
    protected fun observeCategories(forceRefresh: Boolean = false) {
        // Return cached data immediately if available and not forcing refresh
        if (!forceRefresh && cachedCategories != null) {
//            _categories.value = UiState.Success(cachedCategories!!)
            return // Don't trigger network call if we have cache and not forcing refresh
        }

        // We'll only reach here if we need to fetch new data, so set loading state
//        _categories.value = UiState.Loading

        // Cancel existing job if any before starting a new one
        categoriesJob?.cancel()

        categoriesJob = viewModelScope.launch {
            try {
                categoryRepository.observeCategories().collect { result ->
                    result.onSuccess { categories ->
                        cachedCategories = categories
//                        _categories.value = UiState.Success(categories)
                    }.onFailure { error ->
                        if (error !is CancellationException) {
//                            _categories.value = UiState.Error(error.message ?: "Unknown error")
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
//                    _categories.value = UiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Observe doctors with cache support and controlled subscription management
     * @param forceRefresh Set to true to ignore cache and fetch fresh data
     */
    protected fun observeDoctors(forceRefresh: Boolean = false) {
        // Return cached data immediately if available and not forcing refresh
        if (!forceRefresh && cachedDoctors != null) {
            _doctors.value = UiState.Success(cachedDoctors!!)
            return // Don't trigger network call if we have cache and not forcing refresh
        }

        // We'll only reach here if we need to fetch new data, so set loading state
        _doctors.value = UiState.Loading

        // Cancel existing job if any before starting a new one
        doctorsJob?.cancel()

        doctorsJob = viewModelScope.launch {
            try {
                doctorRepository.observeDoctors().collect { result ->
                    result.onSuccess { doctors ->
                        cachedDoctors = doctors
                        _doctors.value = UiState.Success(doctors)
                    }.onFailure { error ->
                        if (error !is CancellationException) {
                            _doctors.value = UiState.Error(error.message ?: "Unknown error")
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _doctors.value = UiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    // Phương thức làm mới danh sách categories
    open fun refreshCategories() {
        // Được override trong các lớp con
    }

    /**
     * Force refresh doctors data
     */
    protected open fun refreshDoctors() {
        observeDoctors(forceRefresh = true)
    }

    /**
     * Force refresh all data
     */
    protected open fun refreshAllData() {
        refreshCategories()
        refreshDoctors()
    }

    override fun onCleared() {
        super.onCleared()
        cachedCategories = null
        cachedDoctors = null
        categoriesJob?.cancel()
        doctorsJob?.cancel()
    }
}