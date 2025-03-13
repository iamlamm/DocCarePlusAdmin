package com.healthtech.doccareplusadmin.ui.widgets.loading

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import androidx.constraintlayout.widget.ConstraintLayout
import com.healthtech.doccareplusadmin.databinding.ViewLoadingBinding

class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)
    private var currentAnimation: Animation? = null

    init {
        // Đảm bảo Lottie được cấu hình tối ưu
        binding.lottieAnimation.apply {
            repeatCount = -1
            speed = 1.0f
            imageAssetsFolder = "images/"
            enableMergePathsForKitKatAndAbove(true)
        }
    }

    /**
     * Hiển thị hoặc ẩn loading view với animation
     * @param isLoading true để hiển thị, false để ẩn
     */
    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            visibility = View.VISIBLE
            binding.lottieAnimation.playAnimation()
        } else {
            binding.lottieAnimation.pauseAnimation()
            visibility = View.GONE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.lottieAnimation.pauseAnimation()
        animate().cancel()
        currentAnimation?.cancel()
    }
}