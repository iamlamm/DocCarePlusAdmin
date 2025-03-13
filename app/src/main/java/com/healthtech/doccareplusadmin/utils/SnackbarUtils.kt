package  com.healthtech.doccareplusadmin.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.Gravity
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.CustomSnackbarBinding

enum class SnackbarType(val lottieRes: Int, val backgroundRes: Int) {
    SUCCESS(R.raw.success_1, R.drawable.bg_snackbar_success),
    ERROR(R.raw.success_2, R.drawable.bg_snackbar_error),
    WARNING(R.raw.success_3, R.drawable.bg_snackbar_warning),
    INFO(R.raw.success_1, R.drawable.bg_snackbar_info)
}

object SnackbarUtils {
    @SuppressLint("RestrictedApi")
    fun showSnackbar(
        view: View,
        message: String,
        type: SnackbarType = SnackbarType.SUCCESS,
        duration: Int = Snackbar.LENGTH_SHORT,
        callback: Snackbar.Callback? = null
    ): Snackbar {
        val snackbar = Snackbar.make(view, "", duration)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

        val params = snackbarLayout.layoutParams
        when (params) {
            is CoordinatorLayout.LayoutParams -> {
                params.width = FrameLayout.LayoutParams.WRAP_CONTENT
                params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            }

            is FrameLayout.LayoutParams -> {
                params.width = FrameLayout.LayoutParams.WRAP_CONTENT
                params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            }
        }
        snackbarLayout.layoutParams = params
        snackbarLayout.removeAllViews()
        snackbarLayout.setBackgroundResource(type.backgroundRes)
        snackbarLayout.setPadding(0, 0, 0, 0)

        val binding = CustomSnackbarBinding.inflate(LayoutInflater.from(view.context))

        binding.lottieIcon.setAnimation(type.lottieRes)
        binding.tvMessage.text = message
        snackbarLayout.addView(binding.root)
        
        // Thêm callback nếu có
        callback?.let { snackbar.addCallback(it) }
        
        snackbar.show()
        return snackbar
    }

    fun showSuccessSnackbar(
        view: View,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        callback: Snackbar.Callback? = null
    ): Snackbar = showSnackbar(view, message, SnackbarType.SUCCESS, duration, callback)

    fun showErrorSnackbar(
        view: View,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        callback: Snackbar.Callback? = null
    ): Snackbar = showSnackbar(view, message, SnackbarType.ERROR, duration, callback)

    fun showWarningSnackbar(
        view: View,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        callback: Snackbar.Callback? = null
    ): Snackbar = showSnackbar(view, message, SnackbarType.WARNING, duration, callback)

    fun showInfoSnackbar(
        view: View,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        callback: Snackbar.Callback? = null
    ): Snackbar = showSnackbar(view, message, SnackbarType.INFO, duration, callback)
}