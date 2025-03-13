package com.healthtech.doccareplusadmin

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.healthtech.doccareplusadmin.utils.Constants
import com.zegocloud.zimkit.services.ZIMKit
import com.zegocloud.zimkit.services.ZIMKitConfig
import dagger.hilt.android.HiltAndroidApp
import im.zego.zim.enums.ZIMErrorCode
import timber.log.Timber

@HiltAndroidApp
class DocCarePlusAdminApplication : Application() {
    private var isCloudinaryInitialized = false

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
//        Firebase.database.setPersistenceEnabled
        Timber.plant(Timber.DebugTree())
        configureZegoCloud()
        configureCloudinary()
    }

    private fun configureZegoCloud() {
        // Khởi tạo ZIMKit
        val zimKitConfig = ZIMKitConfig()
        ZIMKit.initWith(this, Constants.APP_ID, Constants.APP_SIGN, zimKitConfig)
        ZIMKit.initNotifications()

        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (userPrefs.getBoolean("is_logged_in", false)) {
            val userId = userPrefs.getString("user_id", "") ?: ""
            val userName = userPrefs.getString("user_name", "") ?: ""
            val avatarUrl = userPrefs.getString("avatar_url", "")
                ?: "https://res.cloudinary.com/daull03yv/image/upload/v1741287119/polar_bear_q7xdyz.png"

            if (userId.isNotEmpty() && userName.isNotEmpty()) {
                ZIMKit.connectUser(userId, userName, avatarUrl) { error ->
                    if (error.code == ZIMErrorCode.SUCCESS) {
                        Timber.d("ZIMKit auto-reconnected on app launch")
                    }
                }
            }
        }
    }

    private fun configureCloudinary() {
        if (!isCloudinaryInitialized) {
            try {
                val config = HashMap<String, String>()
                config["cloud_name"] = Constants.CLOUDINARY_CLOUD_NAME
                config["api_key"] = Constants.CLOUDINARY_API_KEY
                config["api_secret"] = Constants.CLOUDINARY_API_SECRET

                MediaManager.init(this, config)
                isCloudinaryInitialized = true
                Timber.d("Cloudinary initialized successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize Cloudinary")
            }
        }
    }

//    // Thêm phương thức này để kết nối người dùng với ZIMKit
//    fun connectZIMKit(userId: String, userName: String, callback: () -> Unit) {
//        // Đảm bảo ID là chuỗi
//        val userIdString = userId.trim()
//        val avatarUrl = "https://storage.zego.im/IMKit/avatar/avatar-0.png"
//
//        // Kết nối
//        ZIMKit.connectUser(userIdString, userName, avatarUrl) { error ->
//            if (error.code == ZIMErrorCode.SUCCESS) {
//                Log.d("ZIMKit", "Connected successfully: $userIdString")
//                callback()
//            } else {
//                Log.e("ZIMKit", "Connection failed: ${error.message}")
//            }
//        }
//    }
}