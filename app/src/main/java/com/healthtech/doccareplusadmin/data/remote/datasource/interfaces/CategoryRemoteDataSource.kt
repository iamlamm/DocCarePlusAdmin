package com.healthtech.doccareplusadmin.data.remote.datasource.interfaces

import com.healthtech.doccareplusadmin.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRemoteDataSource {
    fun getCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category): Result<Unit>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun getCategoryById(categoryId: String): Result<Category>
}