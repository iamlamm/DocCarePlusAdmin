package com.healthtech.doccareplusadmin.utils

import android.app.Application
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.zimkit.services.ZIMKit
import com.zegocloud.zimkit.services.ZIMKitConfig
import im.zego.zim.enums.ZIMErrorCode
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ZegoUtils {

    fun initZIMKit(application: Application) {
        val zimKitConfig = ZIMKitConfig()
        ZIMKit.initWith(application, Constants.APP_ID, Constants.APP_SIGN, zimKitConfig)
        ZIMKit.initNotifications()
    }

    suspend fun connectUser(userId: String, userName: String, userAvatar: String) =
        suspendCancellableCoroutine { continuation ->
            ZIMKit.connectUser(userId, userName, userAvatar) { error ->
                when (error.code) {
                    ZIMErrorCode.SUCCESS -> {
                        Timber.d("ZIMKit connected for user: $userName")
                        continuation.resume(Unit)
                    }

                    ZIMErrorCode.USER_HAS_ALREADY_LOGGED -> {
                        ZIMKit.disconnectUser()
                        ZIMKit.connectUser(userId, userName, userAvatar) { retryError ->
                            if (retryError.code == ZIMErrorCode.SUCCESS) {
                                Timber.d("ZIMKit reconnected for user: $userName")
                                continuation.resume(Unit)
                            } else {
                                val exception =
                                    Exception("Failed to reconnect ZIMKit: ${retryError.message}")
                                Timber.e(exception)
                                continuation.resumeWithException(exception)
                            }
                        }
                    }

                    else -> {
                        val exception = Exception("Failed to connect ZIMKit: ${error.message}")
                        Timber.e(exception)
                        continuation.resumeWithException(exception)
                    }
                }
            }

            continuation.invokeOnCancellation {
                ZIMKit.disconnectUser()
            }
        }

    fun initZegoCallService(
        application: Application,
        userId: String,
        userName: String
    ) {
        try {
            val notiConfig = ZegoNotificationConfig().apply {
                sound = "zego_uikit_sound_call"
                channelID = "call_invitation_channel"
                channelName = "Cuộc gọi đến"
            }

            val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig().apply {
                notificationConfig = notiConfig
                incomingCallRingtone = "zego_uikit_sound_call"
                outgoingCallRingtone = "zego_uikit_sound_call_waiting"
                showDeclineButton = true
                innerText.incomingVoiceCallPageTitle = "Cuộc gọi đến từ Bác sĩ"
                innerText.incomingCallPageDeclineButton = "Từ chối"
                innerText.incomingCallPageAcceptButton = "Trả lời"
                endCallWhenInitiatorLeave = true

                provider =
                    ZegoUIKitPrebuiltCallConfigProvider { invitationData ->
                        val isVideoCall = invitationData?.type ==
                                com.zegocloud.uikit.plugin.invitation.ZegoInvitationType.VIDEO_CALL.value

                        if (isVideoCall) {
                            ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall().apply {
                                turnOnCameraWhenJoining = true
                                useSpeakerWhenJoining = true
                            }
                        } else {
                            ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall().apply {
                                turnOnMicrophoneWhenJoining = true
                                useSpeakerWhenJoining = true
                            }
                        }
                    }
            }

            ZegoUIKitPrebuiltCallService.init(
                application,
                Constants.APP_ID,
                Constants.APP_SIGN,
                userId,
                userName,
                callInvitationConfig
            )

            Timber.d("ZegoUIKitPrebuiltCallService initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize ZegoUIKitPrebuiltCallService")
        }
    }

    suspend fun activateDoctor(doctorId: String, doctorName: String, doctorAvatar: String?) {
        try {
            val adminId = ZIMKit.getLocalUser()?.id
            val adminName = ZIMKit.getLocalUser()?.name
            val adminAvatar = ZIMKit.getLocalUser()?.avatarUrl ?: Constants.URL_AVATAR_DEFAULT

            connectUser(
                doctorId,
                doctorName,
                doctorAvatar
                    ?: "https://res.cloudinary.com/daull03yv/image/upload/v1741287119/polar_bear_q7xdyz.png"
            )
            ZIMKit.disconnectUser()
            Timber.d("ZIMKit activated for doctor: $doctorName")

            if (adminId != null && adminName != null) {
                connectUser(adminId, adminName, adminAvatar)
                Timber.d("ZIMKit re-connect zego for admin: $adminName")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error activating Zego for doctor")
            throw e
        }
    }
}