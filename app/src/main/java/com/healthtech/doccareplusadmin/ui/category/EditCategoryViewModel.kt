package com.healthtech.doccareplusadmin.ui.category

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.domain.repository.CategoryRepository
import com.healthtech.doccareplusadmin.domain.repository.StorageRepository
import com.healthtech.doccareplusadmin.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _categoryState = MutableStateFlow<UiState<Category>>(UiState.Idle)
    val categoryState: StateFlow<UiState<Category>> = _categoryState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri = _selectedImageUri.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress = _uploadProgress.asStateFlow()

    private var originalCategory: Category? = null
    private var isNewCategory = true

    init {
        viewModelScope.launch {
            storageRepository.observeUploadProgress().collectLatest { progress ->
                _uploadProgress.value = progress
            }
        }
    }

    fun loadCategory(categoryId: String?) {
        // Reset các state để tránh hiển thị sai khi tạo fragment mới
        _uploadProgress.value = 0
        _saveState.value = UiState.Idle
        
        if (categoryId == null) {
            // Đây là trường hợp thêm mới
            isNewCategory = true
            _categoryState.value = UiState.Success(
                Category(
                    id = System.currentTimeMillis().toInt(),
                    name = "",
                    icon = Constants.URL_CATEGORY_DEFAULT,
                    description = "",
                    code = ""
                )
            )
            return
        }

        // Đây là trường hợp chỉnh sửa
        isNewCategory = false
        _categoryState.value = UiState.Loading

        viewModelScope.launch {
            try {
                // Chuyển đổi String sang Int
                val id = categoryId.toIntOrNull()
                if (id == null) {
                    _categoryState.value = UiState.Error("Invalid category ID")
                    return@launch
                }

                categoryRepository.getCategoryById(id.toString()).onSuccess { category ->
                    originalCategory = category
                    _categoryState.value = UiState.Success(category ?: return@onSuccess)
                }.onFailure { error ->
                    _categoryState.value = UiState.Error(error.message ?: "Failed to load category")
                }
            } catch (e: Exception) {
                _categoryState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun saveCategory(name: String, description: String, code: String) {
        if (name.isBlank()) {
            _saveState.value = UiState.Error("Category name cannot be empty")
            return
        }

        if (code.isBlank()) {
            _saveState.value = UiState.Error("Category code cannot be empty")
            return
        }

        _saveState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val currentCategory = (_categoryState.value as? UiState.Success)?.data 
                    ?: return@launch

                // Xử lý upload ảnh nếu có
                val imageUrl = _selectedImageUri.value?.let { uri ->
                    val fileName = "category_${System.currentTimeMillis()}"
                    storageRepository.uploadCategoryImage(uri, fileName).getOrNull()
                } ?: currentCategory.icon

                val updatedCategory = currentCategory.copy(
                    name = name,
                    description = description,
                    icon = imageUrl,
                    code = code
                )

                val result = if (isNewCategory) {
                    categoryRepository.addCategory(updatedCategory)
                } else {
                    categoryRepository.updateCategory(updatedCategory)
                }

                result.onSuccess {
                    _saveState.value = UiState.Success(Unit)
                }.onFailure { error ->
                    _saveState.value = UiState.Error(error.message ?: "Failed to save category")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving category")
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

    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }
}