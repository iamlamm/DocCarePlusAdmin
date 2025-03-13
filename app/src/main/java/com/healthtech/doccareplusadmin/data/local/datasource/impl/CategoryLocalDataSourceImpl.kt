package  com.healthtech.doccareplusadmin.data.local.datasource.impl

import com.healthtech.doccareplusadmin.data.local.dao.CategoryDao
import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.CategoryLocalDataSource
import com.healthtech.doccareplusadmin.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryLocalDataSourceImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryLocalDataSource {
    override fun getCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    override suspend fun insertCategories(categories: List<CategoryEntity>) {
        categoryDao.insertCategories(categories)
    }

    override suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteCategory(categoryId)
    }

    override suspend fun deleteAllCategories() {
        categoryDao.deleteAllCategories()
    }

    override suspend fun getCategoryById(categoryId: String): CategoryEntity? =
        categoryDao.getCategoryById(categoryId)
}