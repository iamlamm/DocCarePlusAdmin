package com.healthtech.doccareplusadmin.domain.repository

import com.healthtech.doccareplusadmin.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUsers(): Flow<Result<List<User>>>
    
    suspend fun addUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun getUserById(userId: String): Result<User>
}