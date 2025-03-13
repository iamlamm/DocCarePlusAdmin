package com.healthtech.doccareplusadmin.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Khởi tạo navigation controller
        setupNavController()
        
        // Sử dụng thread khác để khởi tạo UI không quan trọng
        lifecycleScope.launch(Dispatchers.Default) {
            // Đảm bảo NavController đã sẵn sàng trước khi thiết lập bottom navigation
            withContext(Dispatchers.Main) {
                setupBottomNavigation()
                setupNavigation()
                
                // THÊM: Theo dõi admin role để cập nhật menu
                observeAdminRole()
            }
            
            // Kiểm tra trạng thái đăng nhập
            withContext(Dispatchers.Main) {
                checkAuthStatus()
            }
        }
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupNavigation() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideBottomNav = when (destination.id) {
                R.id.splashFragment, R.id.loginFragment -> true
                else -> false
            }

            binding.bottomNavigation.visibility = if (hideBottomNav) View.GONE else View.VISIBLE
            
            // Cập nhật selected item trong bottom navigation
            when (destination.id) {
                R.id.dashboardFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
                R.id.allCategoriesFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_categories
                R.id.allDoctorsFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_doctors
                R.id.allUsersFragment -> binding.bottomNavigation.selectedItemId = R.id.nav_users
            }
        }
    }

    private fun setupBottomNavigation() {
        // Hiển thị tất cả các menu items ngay từ đầu
        forceShowAllMenuItems()
        
        // Xử lý click menu item
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            try {
                when (menuItem.itemId) {
                    R.id.nav_dashboard -> {
                        Log.d("MainActivity", "Navigating to dashboard")
                        if (navController.currentDestination?.id != R.id.dashboardFragment) {
                            navController.navigate(R.id.dashboardFragment)
                        }
                        true
                    }
                    R.id.nav_categories -> {
                        if (viewModel.hasPermission("MANAGE_CATEGORIES")) {
                            Log.d("MainActivity", "Navigating to allCategoriesFragment")
                            if (navController.currentDestination?.id != R.id.allCategoriesFragment) {
                                // Nếu đang ở dashboard, sử dụng action
                                if (navController.currentDestination?.id == R.id.dashboardFragment) {
                                    navController.navigate(R.id.action_dashboard_to_allCategories)
                                } else {
                                    // Nếu không, navigate trực tiếp
                                    navController.navigate(R.id.allCategoriesFragment)
                                }
                            }
                            true
                        } else {
                            showPermissionDeniedMessage()
                            false
                        }
                    }
                    R.id.nav_doctors -> {
                        if (viewModel.hasPermission("MANAGE_DOCTORS")) {
                            Log.d("MainActivity", "Navigating to allDoctorsFragment")
                            if (navController.currentDestination?.id != R.id.allDoctorsFragment) {
                                // Nếu đang ở dashboard, sử dụng action
                                if (navController.currentDestination?.id == R.id.dashboardFragment) {
                                    navController.navigate(R.id.action_dashboard_to_allDoctors)
                                } else {
                                    // Nếu không, navigate trực tiếp
                                    navController.navigate(R.id.allDoctorsFragment)
                                }
                            }
                            true
                        } else {
                            showPermissionDeniedMessage()
                            false
                        }
                    }
                    R.id.nav_users -> {
                        if (viewModel.hasPermission("MANAGE_USERS")) {
                            Log.d("MainActivity", "Navigating to allUsersFragment")
                            if (navController.currentDestination?.id != R.id.allUsersFragment) {
                                // Nếu đang ở dashboard, sử dụng action
                                if (navController.currentDestination?.id == R.id.dashboardFragment) {
                                    navController.navigate(R.id.action_dashboard_to_allUsers)
                                } else {
                                    // Nếu không, navigate trực tiếp
                                    navController.navigate(R.id.allUsersFragment)
                                }
                            }
                            true
                        } else {
                            showPermissionDeniedMessage()
                            false
                        }
                    }
                    R.id.nav_reports -> {
                        if (viewModel.hasPermission("VIEW_REPORTS")) {
                            // TODO: Implement navigation to reports fragment
                            showFeatureNotImplementedMessage()
                            true
                        } else {
                            showPermissionDeniedMessage()
                            false
                        }
                    }
                    else -> false
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Navigation error: ${e.message}")
                false
            }
        }
    }

    private fun forceShowAllMenuItems() {
        try {
            val menu = binding.bottomNavigation.menu
            menu.findItem(R.id.nav_dashboard)?.isVisible = true
            menu.findItem(R.id.nav_categories)?.isVisible = true
            menu.findItem(R.id.nav_doctors)?.isVisible = true
            menu.findItem(R.id.nav_users)?.isVisible = true
            menu.findItem(R.id.nav_reports)?.isVisible = true
            Log.d("MainActivity", "Forced all menu items to be visible")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error showing menu items: ${e.message}")
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "You don't have permission to access this feature",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun showFeatureNotImplementedMessage() {
        Toast.makeText(
            this,
            "This feature is not implemented yet",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkAuthStatus() {
        // Chỉ kiểm tra trạng thái đăng nhập khi không ở màn hình splash hoặc login
        lifecycleScope.launch {
            viewModel.isAdminLoggedIn.collect { isLoggedIn ->
                val currentDestination = navController.currentDestination?.id
                if (!isLoggedIn && 
                    currentDestination != R.id.splashFragment && 
                    currentDestination != R.id.loginFragment) {
                    navController.navigate(R.id.loginFragment)
                }
            }
        }
    }

    private fun observeAdminRole() {
        lifecycleScope.launch {
            viewModel.adminRole.collect { role ->
                // Mỗi khi role thay đổi, cập nhật lại menu
                // updateMenuItemsVisibility()
                Log.d("MainActivity", "Admin role updated: ${role?.name}, updating menu visibility")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}