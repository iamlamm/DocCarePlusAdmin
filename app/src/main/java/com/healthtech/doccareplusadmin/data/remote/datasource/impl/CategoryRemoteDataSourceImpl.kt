package com.healthtech.doccareplusadmin.data.remote.datasource.impl

import com.healthtech.doccareplusadmin.data.remote.api.CategoryApi
import com.healthtech.doccareplusadmin.data.remote.api.FirebaseApi
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.CategoryRemoteDataSource
import com.healthtech.doccareplusadmin.domain.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val categoryApi: CategoryApi
) : CategoryRemoteDataSource {
    override fun getCategories(): Flow<List<Category>> = categoryApi.getCategories()

    override suspend fun addCategory(category: Category): Result<Unit> =
        categoryApi.addCategory(category)

    override suspend fun updateCategory(category: Category): Result<Unit> =
        categoryApi.updateCategory(category)

    override suspend fun deleteCategory(categoryId: Int): Result<Unit> =
        categoryApi.deleteCategory(categoryId)

    override suspend fun getCategoryById(categoryId: Int): Result<Category?> =
        categoryApi.getCategoryById(categoryId)
}