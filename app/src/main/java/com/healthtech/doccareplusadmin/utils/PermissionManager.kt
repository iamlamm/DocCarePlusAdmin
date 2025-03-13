package  com.healthtech.doccareplusadmin.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100

        val CALL_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )

        val VIDEO_CALL_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )

        val VOICE_CALL_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )

        fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }

        fun requestPermissions(activity: Activity, permissions: Array<String>) {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                PERMISSION_REQUEST_CODE
            )
        }
    }
}