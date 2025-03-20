package com.healthtech.doccareplusadmin.ui.doctor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.domain.repository.CategoryRepository
import com.healthtech.doccareplusadmin.domain.repository.DoctorRepository
import com.healthtech.doccareplusadmin.domain.repository.StorageRepository
import com.healthtech.doccareplusadmin.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditDoctorViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    private val storageRepository: StorageRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _doctorState = MutableStateFlow<UiState<Doctor>>(UiState.Idle)
    val doctorState: StateFlow<UiState<Doctor>> = _doctorState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    // Thay đổi: Tạo StateFlow cho currentDoctor
    private val _currentDoctor = MutableStateFlow<Doctor?>(null)
    val currentDoctor: StateFlow<Doctor?> = _currentDoctor.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()
    
    // Thêm biến để theo dõi nếu đây là doctor mới
    private var isNewDoctor = true

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            try {
                categoryRepository.observeCategories().collect { result ->
                    result.onSuccess { categoriesList ->
                        _categories.value = categoriesList
                    }.onFailure { error ->
                        Timber.e(error, "Error observing categories")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error setting up category observer")
            }
        }
    }

    fun loadDoctor(doctorId: String?) {
        // Reset các state để tránh hiển thị sai khi tạo fragment mới
        _uploadProgress.value = 0
        _saveState.value = UiState.Idle
        _selectedImageUri.value = null
        
        if (doctorId == null || doctorId.isEmpty()) {
            // Đây là trường hợp thêm mới
            isNewDoctor = true
            // Tạo ID mới sử dụng UUID
            val generatedId = java.util.UUID.randomUUID().toString()
            
            val emptyDoctor = Doctor(
                id = generatedId,
                name = "",
                specialty = "",
                avatar = Constants.URL_DOCTOR_DEFAULT,
                rating = 0F,
                reviews = 0L,
                fee = 0.0,
                biography = "",
                categoryId = 0,
                code = "",
                email = "",
                phoneNumber = "",
                emergencyContact = "",
                address = "",
                available = true
            )
            _doctorState.value = UiState.Success(emptyDoctor)
            _currentDoctor.value = emptyDoctor
            return
        }

        // Đây là trường hợp chỉnh sửa
        isNewDoctor = false
        _doctorState.value = UiState.Loading

        viewModelScope.launch {
            try {
                doctorRepository.getDoctorById(doctorId).onSuccess { doctor ->
                    _currentDoctor.value = doctor
                    _doctorState.value = UiState.Success(doctor!!)
                }.onFailure { error ->
                    _doctorState.value = UiState.Error(error.message ?: "Failed to load doctor")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading doctor")
                _doctorState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun saveDoctor(
        name: String,
        specialty: String,
        fee: String,
        categoryId: Int,
        biography: String,
        code: String,
        email: String,
        phoneNumber: String,
        emergencyContact: String,
        address: String,
        available: Boolean
    ) {
        // Kiểm tra dữ liệu đầu vào
        if (name.isBlank()) {
            _saveState.value = UiState.Error("Doctor name cannot be empty")
            return
        }

        if (code.isBlank()) {
            _saveState.value = UiState.Error("Doctor code cannot be empty")
            return
        }

        // Parse fee
        val feeValue = try {
            if (fee.isNotBlank()) fee.toDouble() else 0.0
        } catch (e: Exception) {
            _saveState.value = UiState.Error("Invalid fee value")
            return
        }

        _saveState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val currentDoctor = (_doctorState.value as? UiState.Success)?.data 
                    ?: return@launch

                // Xử lý upload ảnh nếu có
                val imageUrl = _selectedImageUri.value?.let { uri ->
                    val fileName = "doctor_${System.currentTimeMillis()}"
                    storageRepository.uploadDoctorImage(uri, fileName).getOrNull()
                } ?: currentDoctor.avatar

                val updatedDoctor = currentDoctor.copy(
                    name = name,
                    specialty = specialty,
                    fee = feeValue,
                    categoryId = categoryId,
                    biography = biography,
                    code = code,
                    email = email,
                    phoneNumber = phoneNumber,
                    emergencyContact = emergencyContact,
                    address = address,
                    available = available,
                    avatar = imageUrl
                )

                val result = if (isNewDoctor) {
                    doctorRepository.addDoctor(updatedDoctor)
                } else {
                    doctorRepository.updateDoctor(updatedDoctor)
                }

                result.onSuccess {
                    _saveState.value = UiState.Success(Unit)
                }.onFailure { error ->
                    _saveState.value = UiState.Error(error.message ?: "Failed to save doctor")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving doctor")
                _saveState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = UiState.Idle
    }

    fun resetUploadProgress() {
        _uploadProgress.value = 0
    }

    // Thêm phương thức này để xóa URI ảnh đã chọn sau khi upload thành công
    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }
}