package com.healthtech.doccareplusadmin.ui.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.domain.model.Gender
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.domain.model.UserRole
import com.healthtech.doccareplusadmin.domain.repository.StorageRepository
import com.healthtech.doccareplusadmin.domain.repository.UserRepository
import com.healthtech.doccareplusadmin.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class EditUserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    // User state
    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()

    // Save state
    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    // Selected image URI
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    // Upload progress
    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

    // Currently editing user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Thêm biến để theo dõi nếu đây là user mới
    private var isNewUser = true

    // Load user data (if editing)
    fun loadUser(userId: String?) {
        // Reset các state để tránh hiển thị sai khi tạo fragment mới
        _uploadProgress.value = 0
        _saveState.value = UiState.Idle
        _selectedImageUri.value = null
        
        if (userId == null || userId.isEmpty()) {
            // Đây là trường hợp thêm mới
            isNewUser = true
            // Tạo ID mới
            val generatedId = UUID.randomUUID().toString()
            
            val emptyUser = User(
                id = generatedId,
                name = "",
                email = "",
                phoneNumber = "",
                role = UserRole.PATIENT,
                createdAt = System.currentTimeMillis(),
                avatar = Constants.URL_USER_DEFAULT
                // Các trường khác với giá trị mặc định
            )
            _userState.value = UiState.Success(emptyUser)
            _currentUser.value = emptyUser
            return
        }

        // Đây là trường hợp chỉnh sửa
        isNewUser = false
        _userState.value = UiState.Loading

        viewModelScope.launch {
            try {
                userRepository.getUserById(userId).onSuccess { user ->
                    _currentUser.value = user
                    _userState.value = UiState.Success(user)
                }.onFailure { error ->
                    _userState.value = UiState.Error(error.message ?: "Failed to load user")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user")
                _userState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Save user
    fun saveUser(
        name: String,
        email: String,
        phoneNumber: String,
        role: UserRole,
        age: String?,
        gender: Gender?,
        bloodType: String?,
        height: Int? = null,
        weight: Int? = null,
        about: String? = null
    ) {
        if (name.isBlank()) {
            _saveState.value = UiState.Error("Name cannot be empty")
            return
        }

        if (email.isBlank()) {
            _saveState.value = UiState.Error("Email cannot be empty")
            return
        }

        if (phoneNumber.isBlank()) {
            _saveState.value = UiState.Error("Phone number cannot be empty")
            return
        }

        viewModelScope.launch {
            _saveState.value = UiState.Loading

            try {
                val currentUserData = _currentUser.value 
                    ?: throw IllegalStateException("Cannot save user: current user data not available")

                // Xử lý upload ảnh nếu có
                val avatarUrl = _selectedImageUri.value?.let { uri ->
                    uploadUserImage(uri)
                } ?: currentUserData.avatar
                
                // Log để debug
                Timber.d("Saving user with avatar URL: $avatarUrl")
                
                val userToSave = currentUserData.copy(
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    role = role,
                    age = age?.toIntOrNull(),
                    gender = gender,
                    bloodType = bloodType,
                    height = height,
                    weight = weight,
                    about = about,
                    avatar = avatarUrl?.takeIf { it.isNotBlank() }
                )

                // Lưu user
                val result = if (isNewUser) {
                    userRepository.addUser(userToSave)
                } else {
                    userRepository.updateUser(userToSave)
                }

                result.onSuccess {
                    _saveState.value = UiState.Success(Unit)
                }.onFailure { error ->
                    _saveState.value = UiState.Error(error.message ?: "Failed to save user")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving user")
                _saveState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun uploadUserImage(uri: Uri): String? {
        return try {
            val result = storageRepository.uploadImage(
                folder = Constants.CLOUDINARY_FOLDER_STORE_AVATAR,
                imageUri = uri,
                onProgress = { progress ->
                    _uploadProgress.value = progress
                }
            )

            result.getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading user image")
            null
        }
    }

    // Set selected image
    fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    // Clear selected image
    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }

    // Reset upload progress
    fun resetUploadProgress() {
        _uploadProgress.value = 0
    }

    // Reset save state
    fun resetSaveState() {
        _saveState.value = UiState.Idle
    }
}