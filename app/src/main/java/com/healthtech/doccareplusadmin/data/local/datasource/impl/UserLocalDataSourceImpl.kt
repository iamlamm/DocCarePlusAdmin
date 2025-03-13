package com.healthtech.doccareplusadmin.data.local.datasource.impl

import com.healthtech.doccareplusadmin.data.local.dao.UserDao
import com.healthtech.doccareplusadmin.data.local.datasource.interfaces.UserLocalDataSource
import com.healthtech.doccareplusadmin.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserLocalDataSourceImpl @Inject constructor(
    private val userDao: UserDao
) : UserLocalDataSource {
    override fun getUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    override suspend fun insertUsers(users: List<UserEntity>) {
        userDao.insertUsers(users)
    }

    override suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    override suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    override suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

    override suspend fun getUserById(userId: String): UserEntity? =
        userDao.getUserById(userId)
}