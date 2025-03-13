package com.healthtech.doccareplusadmin.ui.splash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.FragmentSplashBinding
import com.healthtech.doccareplusadmin.utils.AnimationUtils.fadeIn
import com.healthtech.doccareplusadmin.utils.AnimationUtils.fadeInSequentially
import com.healthtech.doccareplusadmin.utils.AnimationUtils.fadeOut
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()
    private var isNavigating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SplashFragment", "onViewCreated")
        
        // Hiển thị animation logo
        startLogoAnimation()
        
        // Hiển thị loading indicator
        if (_binding != null) {
            binding.progressBarSplash.setLoading(true)
        }
        
        // Sử dụng lifecycleScope để đảm bảo coroutine chỉ chạy khi fragment còn sống
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Delay 3 giây để hiển thị splash screen
                delay(3000)
                
                // Kiểm tra trạng thái đăng nhập
                viewModel.checkLoginStatus()
                
                // Chờ thêm 500ms để đảm bảo startDestination đã được cập nhật
                delay(500)
                
                // Lấy giá trị hiện tại của startDestination
                val destination = viewModel.startDestination.value
                Log.d("SplashFragment", "Destination: $destination")
                
                // Kiểm tra xem fragment còn tồn tại không
                if (_binding != null && !isNavigating && isAdded) {
                    isNavigating = true
                    try {
                        // Sử dụng fadeOut animation trước khi navigate
                        fadeOutAndNavigate(destination)
                    } catch (e: Exception) {
                        Log.e("SplashFragment", "Navigation error: ${e.message}")
                        // Fallback navigation nếu animation gặp lỗi
                        navigateToDestination(destination)
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashFragment", "Error in splash: ${e.message}")
            }
        }
    }
    
    private fun startLogoAnimation() {
        try {
            fadeInSequentially(
                binding.imageView,
                binding.imageView2,
                delayBetween = 300
            )
        } catch (e: Exception) {
            Log.e("SplashFragment", "Error starting animation: ${e.message}")
        }
    }
    
    private fun fadeOutAndNavigate(destination: Int) {
        // Tạo một view container để làm animation fade out
        val rootView = binding.root
        
        // Fade out tất cả các view trong splash
        binding.imageView.fadeOut(duration = 500)
        binding.imageView2.fadeOut(duration = 500)
        binding.progressBarSplash.fadeOut(duration = 500)
        
        // Sau khi fade out hoàn tất, thực hiện navigation
        rootView.fadeOut(duration = 800) {
            if (isAdded && !isDetached) {
                navigateToDestination(destination)
            }
        }
    }
    
    private fun navigateToDestination(destination: Int) {
        try {
            when (destination) {
                R.id.loginFragment -> {
                    Log.d("SplashFragment", "Navigating to login")
                    findNavController().navigate(R.id.action_splash_to_login)
                }
                R.id.dashboardFragment -> {
                    Log.d("SplashFragment", "Navigating to dashboard")
                    findNavController().navigate(R.id.action_splash_to_dashboard)
                }
                else -> {
                    Log.d("SplashFragment", "Navigating to login (default)")
                    findNavController().navigate(R.id.action_splash_to_login)
                }
            }
        } catch (e: Exception) {
            Log.e("SplashFragment", "Navigation error in navigateToDestination: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}