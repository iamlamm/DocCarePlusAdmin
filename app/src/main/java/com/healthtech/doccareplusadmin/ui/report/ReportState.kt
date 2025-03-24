package com.healthtech.doccareplusadmin.ui.report

import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.model.AppointmentStats
import com.healthtech.doccareplusadmin.domain.model.OverviewStats
import com.healthtech.doccareplusadmin.domain.model.RevenueStats
import android.net.Uri

sealed class ExportStatus {
    object None : ExportStatus()
    data class Success(val uri: Uri) : ExportStatus()
    object Error : ExportStatus()
}

data class ReportState(
    val isLoading: Boolean = false,
    val overviewStats: OverviewStats? = null,
    val revenueStats: RevenueStats? = null,
    val appointmentStats: AppointmentStats? = null,
    val activities: List<Activity> = emptyList(),
    val error: String? = null,
    val exportSuccess: Boolean = false,
    val exportStatus: ExportStatus = ExportStatus.None,
    val message: String = ""
)