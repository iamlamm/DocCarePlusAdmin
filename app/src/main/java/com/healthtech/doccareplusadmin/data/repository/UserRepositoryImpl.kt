package com.healthtech.doccareplusadmin.data.repository

import android.util.Log
import com.healthtech.doccareplusadmin.data.local.datasource.impl.UserLocalDataSourceImpl
import com.healthtech.doccareplusadmin.data.remote.datasource.impl.UserRemoteDataSourceImpl
import com.healthtech.doccareplusadmin.domain.mapper.toUser
import com.healthtech.doccareplusadmin.domain.mapper.toUserEntity
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

//    override fun observeUsers(): Flow<Result<List<User>>> = channelFlow {
//        // 1. Launch for local data
//        launch {
//            try {
//                localDataSource.getUsers()
//                    .catch { e ->
//                        if (e !is CancellationException) {
//                            send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
//                        }
//                    }
//                    .map { listEntities -> listEntities.map { it.toUser() } }
//                    .collect { localUsers ->
//                        if (localUsers.isNotEmpty()) {
//                            send(Result.success(localUsers))
//                        }
//                    }
//            } catch (e: Exception) {
//                send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
//                Log.e("UserRepository", "Error loading local data", e)
//            }
//        }
//
//        // 2. Launch for remote data with comparison
//        launch {
//            try {
//                var lastLocalUsers: List<User>? = null
//
//                // Keep track of local changes
//                localDataSource.getUsers()
//                    .map { it.map { entity -> entity.toUser() } }
//                    .collect { localUsers ->
//                        lastLocalUsers = localUsers
//                    }
//
//                // Listen to remote changes
//                remoteDataSource.getUsers()
//                    .catch { e ->
//                        if (e !is CancellationException) {
//                            send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
//                        }
//                    }
//                    .collect { remoteUsers ->
//                        // Compare with local data
//                        if (lastLocalUsers != remoteUsers) {
//                            // Update local cache if different
//                            localDataSource.insertUsers(remoteUsers.map { it.toUserEntity() })
//                            // Emit new data
//                            send(Result.success(remoteUsers))
//                        }
//                    }
//            } catch (e: Exception) {
//                send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
//                Log.e("UserRepository", "Error fetching remote data", e)
//            }
//        }
//    }

    override fun observeUsers(): Flow<Result<List<User>>> = callbackFlow {
        try {
            localDataSource.getUsers()
                .catch { e ->
                    if (e !is CancellationException) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
                    }
                }
                .map { listEntities -> listEntities.map { it.toUser() } }
                .collect { localUsers ->
                    if (localUsers.isNotEmpty()) {
                        send(Result.success(localUsers))
                    }

                    // Sau khi emit local data, fetch remote data
                    try {
                        remoteDataSource.getUsers()
                            .catch { e ->
                                if (e !is CancellationException) {
                                    send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                                }
                            }
                            .collect { remoteUsers ->
                                // So sánh với local data
                                if (localUsers != remoteUsers) {
                                    // Update local cache nếu khác
                                    localDataSource.insertUsers(remoteUsers.map { it.toUserEntity() })
                                    // Emit new data
                                    send(Result.success(remoteUsers))
                                }
                            }
                    } catch (e: Exception) {
                        send(Result.failure(Exception("Lỗi khi tải dữ liệu remote: ${e.message}")))
                        Log.e("UserRepository", "Error fetching remote data", e)
                    }
                }
        } catch (e: Exception) {
            send(Result.failure(Exception("Lỗi khi tải dữ liệu local: ${e.message}")))
            Log.e("UserRepository", "Error loading local data", e)
        }
    }

    override suspend fun addUser(user: User): Result<Unit> {
        return try {
            remoteDataSource.addUser(user).onSuccess {
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

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            // Thử lấy từ local trước
            localDataSource.getUserById(userId)?.let {
                return Result.success(it.toUser())
            }

            // Nếu không có trong local, lấy từ remote
            remoteDataSource.getUserById(userId)
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi lấy thông tin người dùng: ${e.message}"))
        }
    }
}