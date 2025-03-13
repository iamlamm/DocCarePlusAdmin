package  com.healthtech.doccareplusadmin.data.service

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.healthtech.doccareplusadmin.domain.service.CloudinaryService
import com.healthtech.doccareplusadmin.domain.service.CloudinaryUploadState
import com.healthtech.doccareplusadmin.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject
import timber.log.Timber

class CloudinaryServiceImpl @Inject constructor(
    private val networkUtils: NetworkUtils
) : CloudinaryService {

    override fun uploadImage(
        imageUri: Uri, 
        folder: String,
        fileName: String?,
        overwrite: Boolean
    ): Flow<CloudinaryUploadState> = callbackFlow {
        if (!networkUtils.isNetworkAvailable()) {
            trySend(CloudinaryUploadState.Error("Không có kết nối mạng"))
            close()
            return@callbackFlow
        }
        
        trySend(CloudinaryUploadState.Loading())
        
        // Tạo tên file nếu không được chỉ định
        val finalFileName = fileName ?: "img_${UUID.randomUUID()}"
        
        try {
            // Thay thế kiểm tra MediaManager.exists() bằng cách an toàn hơn
            try {
                // Nếu MediaManager.get() không ném ra exception, tức là đã được khởi tạo
                val mediaManager = MediaManager.get()
                
                mediaManager.upload(imageUri)
                    .option("public_id", finalFileName)
                    .option("folder", folder)
                    .option("overwrite", overwrite)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Timber.d("Bắt đầu tải lên Cloudinary: $requestId")
                        }
                        
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progressPercent = if (totalBytes > 0) {
                                ((bytes.toDouble() / totalBytes) * 100).toInt()
                            } else {
                                0
                            }
                            trySend(CloudinaryUploadState.Loading(progressPercent))
                        }
                        
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as String
                            Timber.d("Tải lên thành công: $imageUrl")
                            trySend(CloudinaryUploadState.Success(imageUrl))
                            close()
                        }
                        
                        override fun onError(requestId: String, error: ErrorInfo) {
                            Timber.e("Lỗi tải lên Cloudinary: ${error.description}")
                            trySend(CloudinaryUploadState.Error(error.description ?: "Lỗi không xác định khi tải lên"))
                            close()
                        }
                        
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            // Cloudinary sẽ tự động thử lại
                            Timber.d("Lên lịch lại: ${error.description}")
                        }
                    })
                    .dispatch()
            } catch (e: IllegalStateException) {
                // MediaManager chưa được khởi tạo
                Timber.e("Cloudinary chưa được khởi tạo: ${e.message}")
                trySend(CloudinaryUploadState.Error("Cloudinary chưa được khởi tạo. Vui lòng khởi động lại ứng dụng."))
                close()
            }
        } catch (e: Exception) {
            Timber.e("Lỗi không xác định khi tải lên Cloudinary: ${e.message}")
            trySend(CloudinaryUploadState.Error(e.message ?: "Lỗi không xác định"))
            close()
        }
        
        awaitClose {
            Timber.d("Đóng flow upload Cloudinary")
        }
    }.flowOn(Dispatchers.IO)
}