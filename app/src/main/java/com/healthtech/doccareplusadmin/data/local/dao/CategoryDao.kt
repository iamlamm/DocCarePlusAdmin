package  com.healthtech.doccareplusadmin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthtech.doccareplusadmin.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
}