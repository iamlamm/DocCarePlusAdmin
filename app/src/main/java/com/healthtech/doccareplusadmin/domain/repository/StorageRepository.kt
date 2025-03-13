package com.healthtech.doccareplusadmin.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    /**
     * Upload ảnh chuyên khoa lên Cloudinary
     * @param imageUri Uri của ảnh cần upload
     * @param fileName Tên file tùy chọn (nếu null sẽ tạo tên ngẫu nhiên)
     * @return Result chứa URL của ảnh sau khi upload thành công
     */
    suspend fun uploadCategoryImage(imageUri: Uri, fileName: String? = null): Result<String>
    
    /**
     * Upload ảnh bác sĩ lên Cloudinary
     * @param imageUri Uri của ảnh cần upload
     * @param fileName Tên file tùy chọn (nếu null sẽ tạo tên ngẫu nhiên)
     * @return Result chứa URL của ảnh sau khi upload thành công
     */
    suspend fun uploadDoctorImage(imageUri: Uri, fileName: String? = null): Result<String>
    
    /**
     * Upload ảnh người dùng lên Cloudinary
     * @param imageUri Uri của ảnh cần upload
     * @param fileName Tên file tùy chọn (nếu null sẽ tạo tên ngẫu nhiên)
     * @return Result chứa URL của ảnh sau khi upload thành công
     */
    suspend fun uploadUserImage(imageUri: Uri, fileName: String? = null): Result<String>
    
    /**
     * Upload ảnh lên Cloudinary cho bất kỳ folder nào
     * @param folder Tên folder trên Cloudinary 
     * @param imageUri Uri của ảnh cần upload
     * @param fileName Tên file tùy chọn (nếu null sẽ tạo tên ngẫu nhiên)
     * @param onProgress Callback nhận giá trị tiến trình upload (0-100)
     * @return Result chứa URL của ảnh sau khi upload thành công
     */
    suspend fun uploadImage(folder: String, imageUri: Uri, fileName: String? = null, onProgress: ((Int) -> Unit)? = null): Result<String>
    
    /**
     * Theo dõi tiến trình upload ảnh
     * @return Flow chứa phần trăm tiến trình upload (0-100)
     */
    fun observeUploadProgress(): Flow<Int>
}