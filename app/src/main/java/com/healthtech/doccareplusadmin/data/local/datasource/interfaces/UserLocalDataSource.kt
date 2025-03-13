package com.healthtech.doccareplusadmin.data.local.datasource.interfaces

import com.healthtech.doccareplusadmin.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    fun getUsers(): Flow<List<UserEntity>>
    suspend fun insertUsers(users: List<UserEntity>)
    suspend fun insertUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)
    suspend fun deleteUser(userId: String)
    suspend fun deleteAllUsers()
    suspend fun getUserById(userId: String): UserEntity?
}