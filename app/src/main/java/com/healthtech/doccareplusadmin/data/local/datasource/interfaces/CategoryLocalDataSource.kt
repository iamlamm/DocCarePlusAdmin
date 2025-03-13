package com.healthtech.doccareplusadmin.data.local.datasource.interfaces

import com.healthtech.doccareplusadmin.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryLocalDataSource {
    fun getCategories(): Flow<List<CategoryEntity>>
    suspend fun insertCategories(categories: List<CategoryEntity>)
    suspend fun insertCategory(category: CategoryEntity)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(categoryId: String)
    suspend fun deleteAllCategories()
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
}