package com.healthtech.doccareplusadmin.data.repository

import android.net.Uri
import com.healthtech.doccareplusadmin.domain.repository.StorageRepository
import com.healthtech.doccareplusadmin.domain.service.CloudinaryService
import com.healthtech.doccareplusadmin.domain.service.CloudinaryUploadState
import com.healthtech.doccareplusadmin.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val cloudinaryService: CloudinaryService
) : StorageRepository {

    private val _uploadProgress = MutableStateFlow(0)

    override suspend fun uploadCategoryImage(imageUri: Uri, fileName: String?): Result<String> {
        return uploadImage(
            folder = Constants.CLOUDINARY_FOLDER_STORE_CATEGORY,
            imageUri = imageUri,
            fileName = fileName ?: "category_${System.currentTimeMillis()}",
            onProgress = { progress ->
                _uploadProgress.value = progress
            }
        )
    }
    
    override suspend fun uploadDoctorImage(imageUri: Uri, fileName: String?): Result<String> {
        return uploadImage(
            folder = Constants.CLOUDINARY_FOLDER_STORE_DOCTOR,
            imageUri = imageUri,
            fileName = fileName ?: "doctor_${System.currentTimeMillis()}",
            onProgress = { progress ->
                _uploadProgress.value = progress
            }
        )
    }
    
    override suspend fun uploadUserImage(imageUri: Uri, fileName: String?): Result<String> {
        return uploadImage(
            folder = Constants.CLOUDINARY_FOLDER_STORE_AVATAR,
            imageUri = imageUri,
            fileName = fileName ?: "user_${System.currentTimeMillis()}",
            onProgress = { progress ->
                _uploadProgress.value = progress
            }
        )
    }

    override suspend fun uploadImage(
        folder: String, 
        imageUri: Uri, 
        fileName: String?,
        onProgress: ((Int) -> Unit)?
    ): Result<String> {
        return try {
            // Tạo tên file nếu không được chỉ định
            val finalFileName = fileName ?: "${folder}_${System.currentTimeMillis()}"
            
            val result = cloudinaryService.uploadImage(
                imageUri = imageUri,
                folder = folder,
                fileName = finalFileName,
                overwrite = true
            ).first { state ->
                when (state) {
                    is CloudinaryUploadState.Idle -> false
                    is CloudinaryUploadState.Loading -> {
                        onProgress?.invoke(state.progress)
                        _uploadProgress.value = state.progress
                        false
                    }
                    is CloudinaryUploadState.Success -> true
                    is CloudinaryUploadState.Error -> true
                }
            }

            when (result) {
                is CloudinaryUploadState.Success -> Result.success(result.imageUrl)
                is CloudinaryUploadState.Error -> Result.failure(Exception(result.message))
                else -> Result.failure(Exception("Unexpected state during upload"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUploadProgress(): Flow<Int> = _uploadProgress
}