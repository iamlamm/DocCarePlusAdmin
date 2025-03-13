package com.healthtech.doccareplusadmin.utils

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavDirections

/**
 * Thực hiện navigation an toàn cho fragment-to-fragment navigation,
 * tránh crash khi destination không hợp lệ hoặc người dùng click quá nhanh
 * @param actionId ID của action (định nghĩa trong nav_graph)
 */
fun NavController.safeNavigate(actionId: Int) {
    try {
        val action = currentDestination?.getAction(actionId)
        if (action != null) {
            navigate(actionId)
        } else {
            Log.w(
                "Navigation",
                "Action $actionId không tồn tại ở destination: ${currentDestination?.label}"
            )
        }
    } catch (e: Exception) {
        Log.e("Navigation", "Không thể navigate: ${e.message}")
    }
}

/**
 * Thực hiện navigation an toàn cho global navigation (như bottom navigation),
 * cho phép navigate trực tiếp đến destination
 * @param destinationId ID của destination fragment
 */
fun NavController.safeNavigateGlobal(destinationId: Int) {
    try {
        navigate(destinationId)
    } catch (e: Exception) {
        Log.e("Navigation", "Không thể navigate đến destination: ${e.message}")
    }
}

/**
 * Navigate an toàn sử dụng NavDirections
 * @param directions NavDirections object (được tạo bởi Safe Args plugin)
 */
fun NavController.safeNavigate(directions: NavDirections) {
    try {
        val action = currentDestination?.getAction(directions.actionId)
        if (action != null) {
            navigate(directions)
        } else {
            Log.w(
                "Navigation",
                "Action ${directions.actionId} không tồn tại ở destination: ${currentDestination?.label}"
            )
        }
    } catch (e: Exception) {
        Log.e("Navigation", "Không thể navigate với directions: ${e.message}")
    }
}

/**
 * Kiểm tra nếu action tồn tại trước khi navigate
 * (phiên bản đơn giản hơn cho trường hợp đã xử lý try-catch bên ngoài)
 */
fun NavController.navigateIfAvailable(actionId: Int): Boolean {
    val action = currentDestination?.getAction(actionId)
    return if (action != null) {
        navigate(actionId)
        true
    } else {
        false
    }
}

/**
 * Extension function để kiểm tra nếu một action tồn tại mà không thực hiện navigate
 */
fun NavController.canNavigate(actionId: Int): Boolean {
    return currentDestination?.getAction(actionId) != null
}