package  com.healthtech.doccareplusadmin.data.repository

import android.util.Log
import com.healthtech.doccareplusadmin.data.local.datasource.impl.CategoryLocalDataSourceImpl
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.data.remote.datasource.impl.CategoryRemoteDataSourceImpl
import com.healthtech.doccareplusadmin.domain.mapper.toCategory
import com.healthtech.doccareplusadmin.domain.mapper.toCategoryEntity
import com.healthtech.doccareplusadmin.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException


@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: CategoryRemoteDataSourceImpl,
    private val localDataSource: CategoryLocalDataSourceImpl
) : CategoryRepository {
    override fun observeCategories(): Flow<Result<List<Category>>> = channelFlow {
        launch {
            try {
                // Emit local data first
                localDataSource.getCategories().catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                    }
                }
                    .map { listEntities ->
                        listEntities.map { it.toCategory() }
                    }.collect { listCategories ->
                        if (listCategories.isNotEmpty()) {
                            send(Result.success(listCategories))
                        }
                    }
            } catch (e: Exception) {
                send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                Log.e("CategoryRepository", "Error loading local data", e)
            }
        }

        launch {
            try {
                remoteDataSource.getCategories().catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                    }
                }.collect { listCategories ->
                    localDataSource.insertCategories(listCategories.map { it.toCategoryEntity() })
                    send(Result.success(listCategories))
                }
            } catch (e: Exception) {
                send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                Log.e("CategoryRepository", "Error fetching remote data", e)
            }
        }
    }

    override suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            // Thêm vào remote trước
            remoteDataSource.addCategory(category).onSuccess {
                // Nếu thành công thì thêm vào local
                localDataSource.insertCategory(category.toCategoryEntity())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi thêm chuyên khoa: ${e.message}"))
        }
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            // Cập nhật remote trước
            remoteDataSource.updateCategory(category).onSuccess {
                // Nếu thành công thì cập nhật local
                localDataSource.updateCategory(category.toCategoryEntity())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi cập nhật chuyên khoa: ${e.message}"))
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            // Xóa từ remote trước
            remoteDataSource.deleteCategory(categoryId).onSuccess {
                // Nếu thành công thì xóa từ local
                localDataSource.deleteCategory(categoryId)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi xóa chuyên khoa: ${e.message}"))
        }
    }

    override suspend fun getCategoryById(categoryId: String): Result<Category> {
        return try {
            // Thử lấy từ local trước
            localDataSource.getCategoryById(categoryId)?.let {
                return Result.success(it.toCategory())
            }

            // Nếu không có trong local, lấy từ remote
            remoteDataSource.getCategoryById(categoryId).onSuccess { category ->
                // Cache lại vào local
                localDataSource.insertCategory(category.toCategoryEntity())
                Result.success(category)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi lấy thông tin chuyên khoa: ${e.message}"))
        }
    }
}