package com.healthtech.doccareplusadmin

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.healthtech.doccareplusadmin.utils.Constants
import com.healthtech.doccareplusadmin.utils.ZegoUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class DocCarePlusAdminApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isCloudinaryInitialized = false

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
//        Firebase.database.setPersistenceEnabled
        Timber.plant(Timber.DebugTree())
        ZegoUtils.initZIMKit(this)
        configureCloudinary()
        autoReconnectZego()
    }

    private fun autoReconnectZego() {
        val userPrefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (userPrefs.getBoolean("is_logged_in", false)) {
            val userId = userPrefs.getString("user_id", "") ?: ""
            val userName = userPrefs.getString("user_name", "") ?: ""
            val avatarUrl = userPrefs.getString("avatar_url", "")
                ?: "https://res.cloudinary.com/daull03yv/image/upload/v1741287119/polar_bear_q7xdyz.png"

            if (userId.isNotEmpty() && userName.isNotEmpty()) {
                applicationScope.launch {
                    try {
                        ZegoUtils.connectUser(userId, userName, avatarUrl)
                        ZegoUtils.initZegoCallService(
                            this@DocCarePlusAdminApplication,
                            userId,
                            userName
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to auto reconnect Zego")
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

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

//    private fun configureZegoCloud() {
//        // Khởi tạo ZIMKit
//        val zimKitConfig = ZIMKitConfig()
//        ZIMKit.initWith(this, Constants.APP_ID, Constants.APP_SIGN, zimKitConfig)
//        ZIMKit.initNotifications()
//
//        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        if (userPrefs.getBoolean("is_logged_in", false)) {
//            val userId = userPrefs.getString("user_id", "") ?: ""
//            val userName = userPrefs.getString("user_name", "") ?: ""
//            val avatarUrl = userPrefs.getString("avatar_url", "")
//                ?: "https://res.cloudinary.com/daull03yv/image/upload/v1741287119/polar_bear_q7xdyz.png"
//
//            if (userId.isNotEmpty() && userName.isNotEmpty()) {
//                ZIMKit.connectUser(userId, userName, avatarUrl) { error ->
//                    if (error.code == ZIMErrorCode.SUCCESS) {
//                        Timber.d("ZIMKit auto-reconnected on app launch")
//                    }
//                }
//                initZegoCallService(userId, userName)
//            }
//        }
//    }
//
//    fun initZegoCallService(userId: String, userName: String) {
//        try {
//            val notiConfig = ZegoNotificationConfig().apply {
//                sound = "zego_uikit_sound_call"
//                channelID = "call_invitation_channel"
//                channelName = "Cuộc gọi đến"
//            }
//
//            val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
//                .apply {
//                    notificationConfig = notiConfig
//
//                    // Cấu hình âm thanh
//                    incomingCallRingtone = "zego_uikit_sound_call"
//                    outgoingCallRingtone = "zego_uikit_sound_call_waiting"
//
//                    // Các tùy chỉnh giao diện
//                    showDeclineButton = true
//                    innerText.incomingVoiceCallPageTitle = "Cuộc gọi đến từ Bác sĩ"
//                    innerText.incomingCallPageDeclineButton = "Từ chối"
//                    innerText.incomingCallPageAcceptButton = "Trả lời"
//
//                    // Kết thúc cuộc gọi khi người khởi tạo rời đi
//                    endCallWhenInitiatorLeave = true
//
//                    // Cung cấp config cho cuộc gọi
//                    provider = object : ZegoUIKitPrebuiltCallConfigProvider {
//                        override fun requireConfig(invitationData: ZegoCallInvitationData?): ZegoUIKitPrebuiltCallConfig {
//                            val isVideoCall =
//                                invitationData?.type == com.zegocloud.uikit.plugin.invitation.ZegoInvitationType.VIDEO_CALL.value
//                            return if (isVideoCall) {
//                                ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall().apply {
//                                    // Thêm cấu hình video call tùy chỉnh
//                                    turnOnCameraWhenJoining = true
//                                    useSpeakerWhenJoining = true
//                                }
//                            } else {
//                                ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall().apply {
//                                    // Thêm cấu hình voice call tùy chỉnh
//                                    turnOnMicrophoneWhenJoining = true
//                                    useSpeakerWhenJoining = true
//                                }
//                            }
//                        }
//                    }
//                }
//            ZegoUIKitPrebuiltCallService.init(
//                this,
//                Constants.APP_ID,
//                Constants.APP_SIGN,
//                userId,
//                userName,
//                callInvitationConfig
//            )
//
//            Timber.d("ZegoUIKitPrebuiltCallService initialized successfully")
//        } catch (e: Exception) {
//            Timber.e(e, "Failed to initialize ZegoUIKitPrebuiltCallService")
//        }
//    }
}