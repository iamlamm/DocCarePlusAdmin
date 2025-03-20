package com.healthtech.doccareplusadmin.data.remote.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.healthtech.doccareplusadmin.domain.model.Admin
import com.healthtech.doccareplusadmin.domain.model.UserRole
import com.healthtech.doccareplusadmin.utils.NetworkUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApi @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val networkUtils: NetworkUtils
) {
    suspend fun login(email: String, password: String): Result<Admin> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                return Result.failure(Exception("Không có kết nối internet!"))
            }

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Đăng nhập thất bại"))

            // Kiểm tra trong node admins
            val adminSnapshot =
                database.getReference("admins").child(firebaseUser.uid).get().await()
            if (!adminSnapshot.exists()) {
                return Result.failure(Exception("Tài khoản không có quyền truy cập vào ứng dụng Admin"))
            }

            val admin = adminSnapshot.getValue(Admin::class.java)
                ?: return Result.failure(Exception("Không thể đọc dữ liệu admin"))

            if (admin.role != UserRole.ADMIN) {
                return Result.failure(Exception("Tài khoản không có quyền truy cập vào ứng dụng Admin"))
            }

            val finalAdmin = if (admin.permissions.isEmpty()) {
                admin.copy(
                    permissions = when (admin.role) {
                        UserRole.ADMIN -> {
                            mapOf(
                                "MANAGE_CATEGORIES" to true,
                                "MANAGE_DOCTORS" to true,
                                "MANAGE_USERS" to true,
                                "VIEW_REPORTS" to true
                            )
                        }

                        else -> {
                            mapOf(
                                "MANAGE_CATEGORIES" to false,
                                "MANAGE_DOCTORS" to false,
                                "MANAGE_USERS" to false,
                                "VIEW_REPORTS" to false
                            )
                        }
                    }
                )
            } else {
                admin
            }
            return Result.success(finalAdmin)


//            // Parse dữ liệu admin từ snapshot
//            val id = adminSnapshot.child("id").getValue(String::class.java) ?: ""
//            val name = adminSnapshot.child("name").getValue(String::class.java) ?: ""
//            val adminEmail = adminSnapshot.child("email").getValue(String::class.java) ?: ""
//            val avatar = adminSnapshot.child("avatar").getValue(String::class.java)
//            val createdAt = adminSnapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
//
//            // Lấy role (mặc định là ADMIN)
//            val roleStr = adminSnapshot.child("role").getValue(String::class.java) ?: UserRole.ADMIN.name
//            val role = try {
//                UserRole.valueOf(roleStr)
//            } catch (e: Exception) {
//                UserRole.ADMIN
//            }
//
//            // Đọc permissions
//            val permissionsSnapshot = adminSnapshot.child("permissions")
//            val permissions = mutableMapOf<String, Boolean>()
//
//            if (permissionsSnapshot.exists()) {
//                for (permSnapshot in permissionsSnapshot.children) {
//                    permissions[permSnapshot.key!!] = permSnapshot.getValue(Boolean::class.java) ?: false
//                }
//            } else {
//                // Default permissions based on role
//                when (role) {
//                    UserRole.ADMIN -> {
//                        permissions["MANAGE_CATEGORIES"] = true
//                        permissions["MANAGE_DOCTORS"] = true
//                        permissions["MANAGE_USERS"] = true
//                        permissions["VIEW_REPORTS"] = true
//                    }
//                    else -> {
//                        // Nếu không có permissions và role không phải ADMIN
//                        permissions["MANAGE_CATEGORIES"] = false
//                        permissions["MANAGE_DOCTORS"] = false
//                        permissions["MANAGE_USERS"] = false
//                        permissions["VIEW_REPORTS"] = false
//                    }
//                }
//            }
//
//            val admin = Admin(
//                id = id,
//                name = name,
//                email = adminEmail,
//                avatar = avatar,
//                createdAt = createdAt,
//                role = role,
//                permissions = permissions
//            )
//
//            Result.success(admin)

        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Tài khoản không tồn tại"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Email hoặc mật khẩu không chính xác"))
        } catch (e: Exception) {
            Result.failure(Exception("Đã có lỗi xảy ra, vui lòng thử lại sau"))
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}