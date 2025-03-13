package com.healthtech.doccareplusadmin.data.repository

import android.util.Log
import com.healthtech.doccareplusadmin.data.local.datasource.impl.UserLocalDataSourceImpl
import com.healthtech.doccareplusadmin.data.remote.datasource.impl.UserRemoteDataSourceImpl
import com.healthtech.doccareplusadmin.domain.mapper.toUser
import com.healthtech.doccareplusadmin.domain.mapper.toUserEntity
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSourceImpl,
    private val localDataSource: UserLocalDataSourceImpl
) : UserRepository {

    override fun observeUsers(): Flow<Result<List<User>>> = channelFlow {
        launch {
            try {
                // Emit local data first
                localDataSource.getUsers().catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                    }
                }
                    .map { listEntities ->
                        listEntities.map { it.toUser() }
                    }.collect { listUsers ->
                        if (listUsers.isNotEmpty()) {
                            send(Result.success(listUsers))
                        }
                    }
            } catch (e: Exception) {
                send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                Log.e("UserRepository", "Error loading local data", e)
            }
        }

        launch {
            try {
                remoteDataSource.getUsers().catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                    }
                }.collect { listUsers ->
                    localDataSource.insertUsers(listUsers.map { it.toUserEntity() })
                    send(Result.success(listUsers))
                }
            } catch (e: Exception) {
                send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                Log.e("UserRepository", "Error fetching remote data", e)
            }
        }
    }

    override suspend fun addUser(user: User): Result<Unit> {
        return try {
            // Thêm vào remote trước
            remoteDataSource.addUser(user).onSuccess {
                // Nếu thành công thì thêm vào local
                localDataSource.insertUser(user.toUserEntity())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi thêm người dùng: ${e.message}"))
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            // Cập nhật remote trước
            remoteDataSource.updateUser(user).onSuccess {
                // Nếu thành công thì cập nhật local
                localDataSource.updateUser(user.toUserEntity())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi cập nhật người dùng: ${e.message}"))
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Xóa từ remote trước
            remoteDataSource.deleteUser(userId).onSuccess {
                // Nếu thành công thì xóa từ local
                localDataSource.deleteUser(userId)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi xóa người dùng: ${e.message}"))
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            // Thử lấy từ local trước
            localDataSource.getUserById(userId)?.let {
                return Result.success(it.toUser())
            }

            // Nếu không có trong local, lấy từ remote
            remoteDataSource.getUserById(userId).onSuccess { user ->
                // Cache lại vào local
                localDataSource.insertUser(user.toUserEntity())
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi lấy thông tin người dùng: ${e.message}"))
        }
    }
}