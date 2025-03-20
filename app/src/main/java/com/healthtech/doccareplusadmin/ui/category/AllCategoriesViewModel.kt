package com.healthtech.doccareplusadmin.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Chỉ để theo dõi trạng thái đã tải xong chưa
    private val _isInitialDataLoaded = MutableStateFlow(false)
    val isInitialDataLoaded = _isInitialDataLoaded.asStateFlow()

    // Theo dõi thời gian cập nhật cuối
    private var lastRefreshTime = 0L

    // Thêm biến cho tìm kiếm
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Danh sách kết quả tìm kiếm
    private val _searchResults = MutableStateFlow<List<Category>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    // Lưu trữ danh sách gốc
    private var originalCategories = listOf<Category>()

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState = _deleteState.asStateFlow()

    // Thêm biến để theo dõi trạng thái categories
    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories: StateFlow<UiState<List<Category>>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            // Load data ban đầu
            fetchCategories()
        }
    }

    // Hàm fetch categories từ repository
    private fun fetchCategories() {
        _categories.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoryRepository.observeCategories()
                    .collect { result ->
                        result.onSuccess { categoriesList ->
                            _categories.value = UiState.Success(categoriesList)
                            originalCategories = categoriesList
                            
                            if (!_isSearchActive.value) {
                                _searchResults.value = originalCategories
                            } else {
                                filterCategories(_searchQuery.value)
                            }
                            
                            _isInitialDataLoaded.value = true
                        }.onFailure { error ->
                            _categories.value = UiState.Error(error.message ?: "Failed to load categories")
                        }
                    }
            } catch (e: Exception) {
                _categories.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Hàm đặt query tìm kiếm
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterCategories(query)
    }

    // Hàm lọc danh sách
    private fun filterCategories(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = originalCategories
            return
        }

        val filteredList = originalCategories.filter {
            it.name.contains(query, ignoreCase = true)
        }

        _searchResults.value = filteredList
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            clearSearch()
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = originalCategories
    }

    // Hàm này kiểm tra và refresh chỉ khi cần thiết
    fun checkAndRefreshIfNeeded() {
        val currentTime = System.currentTimeMillis()
        // Chỉ refresh nếu đã trôi qua ít nhất 1 phút từ lần cuối
        if (currentTime - lastRefreshTime > 60000) {
            refreshCategories()
        }
    }

    // Hàm này gọi khi người dùng chủ động muốn refresh
    fun refreshCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            lastRefreshTime = System.currentTimeMillis()
            fetchCategories()
        }
    }

    // Thêm method để restore state
    fun restoreState() {
        if (originalCategories.isNotEmpty()) {
            if (!_isSearchActive.value) {
                _searchResults.value = originalCategories
            } else {
                filterCategories(_searchQuery.value)
            }
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            try {
                categoryRepository.deleteCategory(categoryId).onSuccess {
                    _deleteState.value = UiState.Success(Unit)
                    refreshCategories()
                }.onFailure { error ->
                    _deleteState.value = UiState.Error(error.message ?: "Failed to delete category")
                }
            } catch (e: Exception) {
                _deleteState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}