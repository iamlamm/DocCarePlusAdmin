package com.healthtech.doccareplusadmin.domain.repository

import com.healthtech.doccareplusadmin.domain.model.Activity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    /**
     * Lấy tất cả các hoạt động, thường được sắp xếp theo thời gian giảm dần
     */
    suspend fun getAllActivities(): Flow<List<Activity>>
    
    /**
     * Lấy n hoạt động gần đây nhất
     * @param limit Số lượng hoạt động tối đa cần lấy
     */
    suspend fun getRecentActivities(limit: Int): Flow<List<Activity>>
    
    /**
     * Thêm một hoạt động mới vào hệ thống
     * @param activity Đối tượng Activity cần thêm
     * @return true nếu thêm thành công, false nếu có lỗi
     */
    suspend fun addActivity(activity: Activity): Boolean
    
    /**
     * Xóa một hoạt động theo ID
     * @param activityId ID của hoạt động cần xóa
     * @return true nếu xóa thành công, false nếu có lỗi
     */
    suspend fun deleteActivity(activityId: String): Boolean
}