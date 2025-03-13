package com.healthtech.doccareplusadmin.data.remote.datasource.interfaces

import com.healthtech.doccareplusadmin.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRemoteDataSource {
    fun getUsers(): Flow<List<User>>
    suspend fun addUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun getUserById(userId: String): Result<User>
}