package  com.healthtech.doccareplusadmin.domain.service

import android.net.Uri
import com.healthtech.doccareplusadmin.utils.Constants
import kotlinx.coroutines.flow.Flow

interface CloudinaryService {
    fun uploadImage(
        imageUri: Uri,
        folder: String = "",
        fileName: String? = null,
        overwrite: Boolean = true
    ): Flow<CloudinaryUploadState>
}