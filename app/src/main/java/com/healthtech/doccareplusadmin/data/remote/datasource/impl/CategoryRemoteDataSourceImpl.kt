package com.healthtech.doccareplusadmin.data.remote.datasource.impl

import com.healthtech.doccareplusadmin.data.remote.api.FirebaseApi
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.CategoryRemoteDataSource
import com.healthtech.doccareplusadmin.domain.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val firebaseApi: FirebaseApi
) : CategoryRemoteDataSource {
    override fun getCategories(): Flow<List<Category>> = firebaseApi.getCategories()
    
    override suspend fun addCategory(category: Category): Result<Unit> = 
        firebaseApi.addCategory(category)

    override suspend fun updateCategory(category: Category): Result<Unit> = 
        firebaseApi.updateCategory(category)

    override suspend fun deleteCategory(categoryId: String): Result<Unit> = 
        firebaseApi.deleteCategory(categoryId)

    override suspend fun getCategoryById(categoryId: String): Result<Category> = 
        firebaseApi.getCategoryById(categoryId)
}