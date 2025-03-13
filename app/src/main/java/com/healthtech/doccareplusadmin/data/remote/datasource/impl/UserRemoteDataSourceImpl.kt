package com.healthtech.doccareplusadmin.data.remote.datasource.impl

import com.healthtech.doccareplusadmin.data.remote.api.FirebaseApi
import com.healthtech.doccareplusadmin.data.remote.datasource.interfaces.UserRemoteDataSource
import com.healthtech.doccareplusadmin.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRemoteDataSourceImpl @Inject constructor(
    private val firebaseApi: FirebaseApi
) : UserRemoteDataSource {
    override fun getUsers(): Flow<List<User>> = firebaseApi.getUsers()
    
    override suspend fun addUser(user: User): Result<Unit> = 
        firebaseApi.addUser(user)

    override suspend fun updateUser(user: User): Result<Unit> = 
        firebaseApi.updateUser(user)

    override suspend fun deleteUser(userId: String): Result<Unit> = 
        firebaseApi.deleteUser(userId)

    override suspend fun getUserById(userId: String): Result<User> = 
        firebaseApi.getUserById(userId)
}