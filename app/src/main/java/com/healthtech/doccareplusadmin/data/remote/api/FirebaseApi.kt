package  com.healthtech.doccareplusadmin.data.remote.api

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Admin
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.domain.model.Gender
import com.healthtech.doccareplusadmin.domain.model.TimeSlot
import com.healthtech.doccareplusadmin.domain.model.TimePeriod
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.domain.model.UserRole
import com.healthtech.doccareplusadmin.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * callbackFlow {}: Mở một luồng để lắng nghe sự kiện.
 * trySend(value): Gửi giá trị vào Flow.
 * awaitClose {}: Đóng Flow khi không còn lắng nghe nữa.
 */

@Singleton
class FirebaseApi @Inject constructor(
    private val database: FirebaseDatabase
) {
    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val categoriesRef = database.getReference("categories")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<Category>()
                snapshot.children.forEach { categorySnapshot ->
                    val id = categorySnapshot.key ?: return@forEach
                    val name = categorySnapshot.child("name").getValue(String::class.java)
                        ?: return@forEach
                    val icon = categorySnapshot.child("icon").getValue(String::class.java)
                        ?: return@forEach
                    val description =
                        categorySnapshot.child("description").getValue(String::class.java)
                    val code = categorySnapshot.child("code").getValue(String::class.java)

                    categoryList.add(
                        Category(
                            id = id.toInt(),
                            name = name,
                            icon = icon,
                            description = description ?: "",
                            code = code ?: ""
                        )
                    )
                }
                trySend(categoryList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR LOADING CATEGORY", error.message)
            }
        }
        categoriesRef.addValueEventListener(listener)
        awaitClose {
            categoriesRef.removeEventListener(listener)
            println("Flow getCategories đóng")
        }
    }

    suspend fun getCategoryById(categoryId: String): Result<Category> {
        return try {
            val snapshot = database.getReference("categories")
                .get()
                .await()

            if (!snapshot.hasChild(categoryId)) {
                return Result.failure(Exception("Category not found"))
            }

            val categorySnapshot = snapshot.child(categoryId)
            val name = categorySnapshot.child("name").getValue(String::class.java)
            val icon = categorySnapshot.child("icon").getValue(String::class.java)
            val description = categorySnapshot.child("description").getValue(String::class.java)
            val code = categorySnapshot.child("code").getValue(String::class.java)

            if (name != null && icon != null) {
                Result.success(
                    Category(
                        id = categoryId.toInt(),  // Giữ nguyên ID dạng String
                        name = name,
                        icon = icon,
                        description = description ?: "",
                        code = code ?: ""
                    )
                )
            } else {
                Result.failure(Exception("Invalid category data"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error getting category: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            val categoriesRef = database.getReference("categories")

            // Tạo một key mới nếu id chưa được cung cấp
            val categoryId = category.id ?: categoriesRef.push().key
            ?: throw Exception("Failed to generate category ID")

            // Tạo map chứa đầy đủ dữ liệu category với kiểu dữ liệu phù hợp
            val categoryData = mapOf(
                categoryId.toString() to mapOf(
                    "name" to (category.name ?: ""),
                    "icon" to (category.icon ?: ""),
                    "description" to (category.description ?: ""),
                    "code" to (category.code ?: "")
                )
            )

            // Update vào database
            categoriesRef.updateChildren(categoryData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error adding category: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            val categoryId = category.id ?: throw Exception("Category ID is required")

            val categoriesRef = database.getReference("categories")

            // Kiểm tra category tồn tại
            val snapshot = categoriesRef.get().await()
            if (!snapshot.hasChild(categoryId.toString())) {
                return Result.failure(Exception("Category not found"))
            }

            // Cập nhật đầy đủ dữ liệu
            val updates = mapOf(
                "$categoryId/name" to category.name,
                "$categoryId/icon" to category.icon,
                "$categoryId/description" to (category.description ?: ""),
                "$categoryId/code" to (category.code ?: "")
            )

            categoriesRef.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error updating category: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            val categoriesRef = database.getReference("categories")

            // Kiểm tra category tồn tại
            val snapshot = categoriesRef.get().await()
            if (!snapshot.hasChild(categoryId)) {
                return Result.failure(Exception("Category not found"))
            }

            // Xóa category
            val updates = mapOf<String, Any?>(
                categoryId to null
            )

            categoriesRef.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error deleting category: ${e.message}")
            Result.failure(e)
        }
    }

    fun getDoctors(): Flow<List<Doctor>> = callbackFlow {
        val doctorsRef = database.getReference("doctors")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctorList = mutableListOf<Doctor>()

                // Duyệt qua các doctor (giờ là object thay vì array)
                for (doctorSnapshot in snapshot.children) {
                    val id = doctorSnapshot.key ?: continue

                    try {
                        // Đọc các thuộc tính của doctor
                        val name = doctorSnapshot.child("name").getValue(String::class.java) ?: ""
                        val specialty =
                            doctorSnapshot.child("specialty").getValue(String::class.java) ?: ""
                        val avatar = doctorSnapshot.child("avatar").getValue(String::class.java)
                            ?: Constants.URL_DOCTOR_DEFAULT
                        val rating =
                            doctorSnapshot.child("rating").getValue(Double::class.java)?.toFloat()
                                ?: 0F
                        val reviews =
                            doctorSnapshot.child("reviews").getValue(Long::class.java) ?: 0
                        val fee = doctorSnapshot.child("fee").getValue(Double::class.java) ?: 0.0
                        val biography =
                            doctorSnapshot.child("biography").getValue(String::class.java) ?: ""
                        val categoryId =
                            doctorSnapshot.child("categoryId").getValue(Long::class.java)?.toInt()
                                ?: 0
                        val code = doctorSnapshot.child("code").getValue(String::class.java) ?: ""
                        val email = doctorSnapshot.child("email").getValue(String::class.java) ?: ""
                        val phoneNumber =
                            doctorSnapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                        val emergencyContact =
                            doctorSnapshot.child("emergencyContact").getValue(String::class.java)
                                ?: ""
                        val address =
                            doctorSnapshot.child("address").getValue(String::class.java) ?: ""
                        val available =
                            doctorSnapshot.child("available").getValue(Boolean::class.java) ?: true

                        // Tạo đối tượng Doctor và thêm vào danh sách
                        doctorList.add(
                            Doctor(
                                id = id,
                                name = name,
                                specialty = specialty,
                                avatar = avatar,
                                rating = rating,
                                reviews = reviews,
                                fee = fee,
                                biography = biography,
                                categoryId = categoryId,
                                code = code,
                                email = email,
                                phoneNumber = phoneNumber,
                                emergencyContact = emergencyContact,
                                address = address,
                                available = available
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseApi", "Error parsing doctor data: ${e.message}")
                    }
                }

                // Loại bỏ trùng lặp và gửi dữ liệu
                val uniqueDoctors = doctorList.distinctBy { it.id }
                if (uniqueDoctors.size != doctorList.size) {
                    Log.w(
                        "FirebaseApi",
                        "Detected duplicates in doctors data: ${doctorList.size} -> ${uniqueDoctors.size}"
                    )
                }
                trySend(uniqueDoctors)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR LOADING DOCTORS", error.message)
            }
        }

        doctorsRef.addValueEventListener(listener)
        awaitClose {
            doctorsRef.removeEventListener(listener)
            println("Flow getDoctors đóng")
        }
    }

    suspend fun getDoctorById(doctorId: String): Result<Doctor> {
        return try {
            val snapshot = database.getReference("doctors")
                .child(doctorId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("Doctor not found"))
            }

            val name = snapshot.child("name").getValue(String::class.java)
            val specialty = snapshot.child("specialty").getValue(String::class.java)
            val avatar = snapshot.child("avatar").getValue(String::class.java)
            val rating = snapshot.child("rating").getValue(Double::class.java)?.toFloat() ?: 0F
            val reviews = snapshot.child("reviews").getValue(Long::class.java)
            val fee = snapshot.child("fee").getValue(Double::class.java)
            val biography = snapshot.child("biography").getValue(String::class.java)
            val categoryId = snapshot.child("categoryId").getValue(Long::class.java)?.toInt()
            val code = snapshot.child("code").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)
            val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java)
            val emergencyContact = snapshot.child("emergencyContact").getValue(String::class.java)
            val address = snapshot.child("address").getValue(String::class.java)
            val available = snapshot.child("available").getValue(Boolean::class.java)

            if (name != null && specialty != null) {
                Result.success(
                    Doctor(
                        id = doctorId,
                        name = name,
                        specialty = specialty,
                        avatar = avatar ?: Constants.URL_DOCTOR_DEFAULT,
                        rating = rating,
                        reviews = reviews ?: 0,
                        fee = fee ?: 0.0,
                        biography = biography ?: "",
                        categoryId = categoryId ?: 0,
                        code = code ?: "",
                        email = email ?: "",
                        phoneNumber = phoneNumber ?: "",
                        emergencyContact = emergencyContact ?: "",
                        address = address ?: "",
                        available = available ?: true
                    )
                )
            } else {
                Result.failure(Exception("Invalid doctor data"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error getting doctor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addDoctor(doctor: Doctor): Result<Unit> {
        return try {
            val doctorsRef = database.getReference("doctors")

            // Sử dụng ID từ doctor hoặc tạo ID mới nếu chưa có
            val doctorId = doctor.id.takeIf { it.isNotEmpty() } ?: doctorsRef.push().key
            ?: throw Exception("Failed to generate doctor ID")

            // Tạo map chứa dữ liệu doctor
            val doctorData = mapOf(
                "name" to doctor.name,
                "specialty" to doctor.specialty,
                "avatar" to doctor.avatar,
                "rating" to doctor.rating,
                "reviews" to doctor.reviews,
                "fee" to doctor.fee,
                "biography" to doctor.biography,
                "categoryId" to doctor.categoryId,
                "code" to doctor.code,
                "email" to doctor.email,
                "phoneNumber" to doctor.phoneNumber,
                "emergencyContact" to doctor.emergencyContact,
                "address" to doctor.address,
                "available" to doctor.available
            )

            // Thêm doctor vào database
            doctorsRef.child(doctorId).setValue(doctorData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error adding doctor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateDoctor(doctor: Doctor): Result<Unit> {
        return try {
            val doctorId =
                doctor.id.takeIf { it.isNotEmpty() } ?: throw Exception("Doctor ID is required")

            val doctorsRef = database.getReference("doctors")

            // Kiểm tra doctor tồn tại
            val snapshot = doctorsRef.child(doctorId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Doctor not found"))
            }

            // Cập nhật dữ liệu
            val doctorData = mapOf(
                "name" to doctor.name,
                "specialty" to doctor.specialty,
                "avatar" to doctor.avatar,
                "rating" to doctor.rating,
                "reviews" to doctor.reviews,
                "fee" to doctor.fee,
                "biography" to doctor.biography,
                "categoryId" to doctor.categoryId,
                "code" to doctor.code,
                "email" to doctor.email,
                "phoneNumber" to doctor.phoneNumber,
                "emergencyContact" to doctor.emergencyContact,
                "address" to doctor.address,
                "available" to doctor.available
            )

            doctorsRef.child(doctorId).updateChildren(doctorData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error updating doctor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteDoctor(doctorId: String): Result<Unit> {
        return try {
            val doctorsRef = database.getReference("doctors")

            // Kiểm tra doctor tồn tại
            val snapshot = doctorsRef.child(doctorId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Doctor not found"))
            }

            // Xóa doctor
            doctorsRef.child(doctorId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error deleting doctor: ${e.message}")
            Result.failure(e)
        }
    }

    fun getTimeSlots(): Flow<List<TimeSlot>> = callbackFlow {
        val timeSlotsRef = database.getReference("timeSlots")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Thêm log để debug
                Log.d("FirebaseApi", "Connection successful: ${snapshot.exists()}")
                Log.d("FirebaseApi", "Raw data: ${snapshot.value}")

                val timeSlotList = mutableListOf<TimeSlot>()

                if (!snapshot.exists()) {
                    Log.d("FirebaseApi", "No data exists")
                    trySend(timeSlotList)
                    return
                }

                // Parse theo cấu trúc trong database.json
                // timeSlots: { morning: [...], afternoon: [...], evening: [...] }
                val periods = listOf("morning", "afternoon", "evening")

                periods.forEach { periodName ->
                    val periodSnapshot = snapshot.child(periodName)
                    if (!periodSnapshot.exists()) {
                        Log.d("FirebaseApi", "Không có dữ liệu cho period: $periodName")
                        return@forEach
                    }

                    val timePeriod = when (periodName) {
                        "morning" -> TimePeriod.MORNING
                        "afternoon" -> TimePeriod.AFTERNOON
                        "evening" -> TimePeriod.EVENING
                        else -> null
                    } ?: return@forEach

                    periodSnapshot.children.forEach { slotSnapshot ->
                        try {
                            val id = slotSnapshot.child("id").getValue(Int::class.java)
                            val startTime =
                                slotSnapshot.child("startTime").getValue(String::class.java)
                            val endTime = slotSnapshot.child("endTime").getValue(String::class.java)

                            if (id == null || startTime == null || endTime == null) {
                                Log.e(
                                    "FirebaseApi",
                                    "Thiếu thông tin cho time slot trong $periodName"
                                )
                                return@forEach
                            }

                            val timeSlot = TimeSlot(
                                id = id,
                                startTime = startTime,
                                endTime = endTime,
                                period = timePeriod
                            )

                            Log.d("FirebaseApi", "Đã parse được slot: $timeSlot")
                            timeSlotList.add(timeSlot)
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseApi",
                                "Lỗi khi parse slot trong $periodName: ${e.message}"
                            )
                        }
                    }
                }

                Log.d("FirebaseApi", "Total parsed slots: ${timeSlotList.size}")
                trySend(timeSlotList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseApi", "Firebase error: ${error.message}")
                Log.e("FirebaseApi", "Error details: ${error.details}")
                close(error.toException())
            }
        }

        // Thêm log để xác nhận listener được đăng ký
        Log.d("FirebaseApi", "Registering listener for timeSlots")
        timeSlotsRef.addValueEventListener(listener)

        awaitClose {
            timeSlotsRef.removeEventListener(listener)
            Log.d("FirebaseApi", "Listener removed")
        }
    }

    fun getUsers(): Flow<List<User>> = callbackFlow {
        val usersRef = database.getReference("users")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    try {
                        val id = userSnapshot.key ?: continue
                        val name = userSnapshot.child("name").getValue(String::class.java) ?: continue
                        val email = userSnapshot.child("email").getValue(String::class.java) ?: continue
                        val phoneNumber =
                            userSnapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                        val role = userSnapshot.child("role").getValue(String::class.java)
                            ?: UserRole.PATIENT.name
                        val createdAt = userSnapshot.child("createdAt").getValue(Long::class.java)
                            ?: System.currentTimeMillis()
                        
                        // Xử lý cẩn thận trường avatar
                        val avatar = userSnapshot.child("avatar").getValue(String::class.java)?.let {
                            if (it.isBlank()) null else it
                        }
                        
                        // Log để debug
                        Log.d("FirebaseApi", "User $id avatar: $avatar")
                        
                        val height = userSnapshot.child("height").getValue(Int::class.java)
                        val weight = userSnapshot.child("weight").getValue(Int::class.java)
                        val age = userSnapshot.child("age").getValue(Int::class.java)
                        val bloodType = userSnapshot.child("bloodType").getValue(String::class.java)
                        val about = userSnapshot.child("about").getValue(String::class.java)
                        val gender = userSnapshot.child("gender").getValue(String::class.java)

                        userList.add(User(
                            id = id,
                            name = name,
                            email = email,
                            phoneNumber = phoneNumber,
                            role = UserRole.valueOf(role),
                            createdAt = createdAt,
                            avatar = avatar,
                            height = height,
                            weight = weight,
                            age = age,
                            bloodType = bloodType,
                            about = about,
                            gender = gender?.let { Gender.valueOf(it) }
                        ))
                    } catch (e: Exception) {
                        Log.e("FirebaseApi", "Error parsing user data: ${e.message}", e)
                    }
                }
                
                // Thêm distinctBy trước khi emit để loại bỏ trùng lặp
                val uniqueUsers = userList.distinctBy { it.id }
                if (uniqueUsers.size != userList.size) {
                    Log.w(
                        "FirebaseApi",
                        "Detected duplicates in Firebase data: ${userList.size} -> ${uniqueUsers.size}"
                    )
                }
                trySend(uniqueUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseApi", "Error loading users: ${error.message}")
            }
        }
        usersRef.addValueEventListener(listener)
        awaitClose {
            usersRef.removeEventListener(listener)
            Log.d("FirebaseApi", "Users flow closed")
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val snapshot = database.getReference("users")
                .child(userId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("User not found"))
            }

            val name = snapshot.child("name").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)
            val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java)
            val role = snapshot.child("role").getValue(String::class.java)
            val createdAt = snapshot.child("createdAt").getValue(Long::class.java)
            val avatar = snapshot.child("avatar").getValue(String::class.java)
            val height = snapshot.child("height").getValue(Int::class.java)
            val weight = snapshot.child("weight").getValue(Int::class.java)
            val age = snapshot.child("age").getValue(Int::class.java)
            val bloodType = snapshot.child("bloodType").getValue(String::class.java)
            val about = snapshot.child("about").getValue(String::class.java)
            val gender = snapshot.child("gender").getValue(String::class.java)

            if (name != null && email != null && phoneNumber != null && role != null) {
                Result.success(User(
                    id = userId,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    role = UserRole.valueOf(role),
                    createdAt = createdAt ?: System.currentTimeMillis(),
                    avatar = avatar,
                    height = height,
                    weight = weight,
                    age = age,
                    bloodType = bloodType,
                    about = about,
                    gender = gender?.let { Gender.valueOf(it) }
                ))
            } else {
                Result.failure(Exception("Invalid user data"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error getting user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val usersRef = database.getReference("users")

            // Kiểm tra user tồn tại
            val snapshot = usersRef.child(user.id).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("User not found"))
            }

            // Cập nhật đầy đủ dữ liệu
            val updates = mapOf(
                "name" to user.name,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "role" to user.role.name,
                "createdAt" to user.createdAt,
                "avatar" to user.avatar,
                "height" to user.height,
                "weight" to user.weight,
                "age" to user.age,
                "bloodType" to user.bloodType,
                "about" to user.about,
                "gender" to user.gender?.name
            )

            usersRef.child(user.id).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error updating user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val usersRef = database.getReference("users")

            // Kiểm tra user tồn tại
            val snapshot = usersRef.child(userId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("User not found"))
            }

            // Xóa user
            usersRef.child(userId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error deleting user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addUser(user: User): Result<Unit> {
        return try {
            val usersRef = database.getReference("users")

            // Kiểm tra xem ID đã được cung cấp hay chưa
            val userId = if (user.id.isNotEmpty()) {
                user.id
            } else {
                // Tạo một key mới nếu id chưa được cung cấp
                usersRef.push().key ?: throw Exception("Failed to generate user ID")
            }

            // Map thông tin người dùng
            val userData = mapOf(
                userId to mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "phoneNumber" to user.phoneNumber,
                    "role" to user.role.name,
                    "createdAt" to user.createdAt,
                    "avatar" to user.avatar,
                    "height" to user.height,
                    "weight" to user.weight,
                    "age" to user.age,
                    "bloodType" to user.bloodType,
                    "about" to user.about,
                    "gender" to user.gender?.name
                )
            )

            // Update vào database
            usersRef.updateChildren(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseApi", "Error adding user: ${e.message}")
            Result.failure(e)
        }
    }
}