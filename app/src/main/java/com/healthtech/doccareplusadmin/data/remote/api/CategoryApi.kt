package com.healthtech.doccareplusadmin.data.remote.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Category
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryApi @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val TAG = "CategoryApi"

    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val categoryRef = database.getReference("categories")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<Category>()
                for (categorySnapshot in snapshot.children) {
                    categorySnapshot.getValue(Category::class.java)?.let { category ->
                        categoryList.add(category)
                    }
                }
                trySend(categoryList)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(TAG, "Error getCategories: ${error.message}")
            }
        }
        categoryRef.addValueEventListener(listener)
        awaitClose { categoryRef.removeEventListener(listener) }
    }

    suspend fun getCategoryById(categoryId: Int): Result<Category?> {
        val categoriesRef = database.getReference("categories")
        return try {
            val snapshot = categoriesRef.get().await()

            if (!snapshot.exists()) {
                Timber.w(TAG, "Categories node not found")
                return Result.failure(Exception("Categories not found"))
            }

            var foundCategory: Category? = null
            for (categorySnapshot in snapshot.children) {
                val category = categorySnapshot.getValue(Category::class.java)
                if (category?.id == categoryId) {
                    foundCategory = category
                    break
                }
            }

            if (foundCategory == null) {
                Timber.w(TAG, "Category not found with id: $categoryId")
                return Result.failure(Exception("Category not found"))
            }

            Result.success(foundCategory)
        } catch (e: Exception) {
            Timber.e(TAG, "Error getCategoryById: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addCategory(category: Category): Result<Unit> {
        val categoriesRef = database.getReference("categories")
        return try {
            // Kiểm tra xem category với id này đã tồn tại chưa
            val snapshot = categoriesRef.get().await()

            // Kiểm tra id đã tồn tại
            for (categorySnapshot in snapshot.children) {
                val existingCategory = categorySnapshot.getValue(Category::class.java)
                if (existingCategory?.id == category.id) {
                    Timber.w(TAG, "Category already exists with id: ${category.id}")
                    return Result.failure(Exception("Category already exists"))
                }
            }

            // Thêm category mới vào cuối array
            val newIndex = snapshot.childrenCount.toInt()
            categoriesRef.child(newIndex.toString()).setValue(category).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(TAG, "Error addCategory: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: Category): Result<Unit> {
        val categoriesRef = database.getReference("categories")
        return try {
            val snapshot = categoriesRef.get().await()

            // Tìm index của category cần update
            var categoryIndex = -1
            for (i in 0 until snapshot.childrenCount.toInt()) {
                val categorySnapshot = snapshot.child(i.toString())
                val existingCategory = categorySnapshot.getValue(Category::class.java)
                if (existingCategory?.id == category.id) {
                    categoryIndex = i
                    break
                }
            }

            if (categoryIndex == -1) {
                Timber.w(TAG, "Category not found with id: ${category.id}")
                return Result.failure(Exception("Category not found"))
            }

            // Update category tại index tìm được
            categoriesRef.child(categoryIndex.toString()).setValue(category).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(TAG, "Error updateCategory: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        val categoriesRef = database.getReference("categories")
        return try {
            val snapshot = categoriesRef.get().await()

            // Tìm index của category cần xóa
            var categoryIndex = -1
            val categories = mutableListOf<Category>()

            // Lấy tất cả categories và tìm index cần xóa
            for (i in 0 until snapshot.childrenCount.toInt()) {
                val categorySnapshot = snapshot.child(i.toString())
                val category = categorySnapshot.getValue(Category::class.java)
                if (category != null) {
                    if (category.id == categoryId) {
                        categoryIndex = i
                    } else {
                        categories.add(category)
                    }
                }
            }

            if (categoryIndex == -1) {
                Timber.w(TAG, "Category not found with id: $categoryId")
                return Result.failure(Exception("Category not found"))
            }

            // Xóa toàn bộ array cũ
            categoriesRef.removeValue().await()

            // Ghi lại array mới không có category bị xóa
            for (i in categories.indices) {
                categoriesRef.child(i.toString()).setValue(categories[i]).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(TAG, "Error deleteCategory: ${e.message}")
            Result.failure(e)
        }
    }
}