package com.healthtech.doccareplusadmin.data.repository

import com.healthtech.doccareplusadmin.data.remote.api.ActivityApi
import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val activityApi: ActivityApi
) : ActivityRepository {
    override suspend fun getAllActivities(): Flow<List<Activity>> {
        return activityApi.getAllActivities()
    }

    override suspend fun getRecentActivities(limit: Int): Flow<List<Activity>> {
        return activityApi.getRecentActivities(limit)
    }

    override suspend fun addActivity(activity: Activity): Boolean {
        return activityApi.addActivity(activity)
    }

    override suspend fun deleteActivity(activityId: String): Boolean {
        return activityApi.deleteActivity(activityId)
    }
}