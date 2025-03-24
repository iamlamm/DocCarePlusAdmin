package com.healthtech.doccareplusadmin.ui.report

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.data.remote.api.ReportApi
import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.model.AppointmentStats
import com.healthtech.doccareplusadmin.domain.model.OverviewStats
import com.healthtech.doccareplusadmin.domain.model.RevenueStats
import com.healthtech.doccareplusadmin.ui.report.chart.ChartUtils
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportApi: ReportApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportState())
    val uiState = _uiState.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    init {
        // Collect flows from ReportApi
        viewModelScope.launch {
            reportApi.getOverviewStats().collect { stats ->
                _uiState.update { currentState ->
                    currentState.copy(overviewStats = stats)
                }
            }
        }

        viewModelScope.launch {
            reportApi.getRevenueStats().collect { stats ->
                _uiState.update { currentState ->
                    currentState.copy(revenueStats = stats)
                }
            }
        }

        viewModelScope.launch {
            reportApi.getAppointmentStats().collect { stats ->
                _uiState.update { currentState ->
                    currentState.copy(appointmentStats = stats)
                }
            }
        }
    }

    private fun loadInitialData() {
        // Set default date range to last 7 days
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (7 * 24 * 60 * 60 * 1000)
        _selectedDateRange.value = Pair(startDate, endDate)
        loadReportData(startDate, endDate)
    }

    fun loadReportData(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Load activities for the selected date range
                reportApi.getActivitiesForReport(startDate, endDate)
                    .onSuccess { activities ->
                        _uiState.value = _uiState.value.copy(
                            activities = activities,
                            isLoading = false
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Lỗi tải dữ liệu: ${e.message}"
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi tải dữ liệu: ${e.message}"
                )
            }
        }
    }

    fun updateDateRange(startDate: Long, endDate: Long) {
        _selectedDateRange.value = Pair(startDate, endDate)
        loadReportData(startDate, endDate)
    }

    fun updateSelectedTab(position: Int) {
        _selectedTab.value = position
    }

    fun exportReport(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val fileName = "DocCarePlus_Report_${getCurrentDateTime()}.pdf"
                
                // Lưu file vào thư mục Downloads của ứng dụng
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                file.parentFile?.mkdirs()

                // Tạo PDF file
                FileOutputStream(file).use { outputStream ->
                    generatePdfDocument(context, outputStream)
                }

                // Log đường dẫn file để debug
                Timber.d("PDF file path: ${file.absolutePath}")

                // Lấy URI thông qua FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportStatus = ExportStatus.Success(uri),
                    message = "Xuất báo cáo thành công\nĐường dẫn: ${file.absolutePath}"
                )

                showExportNotification(context, uri, fileName)

            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exportStatus = ExportStatus.Error,
                    message = "Lỗi xuất báo cáo: ${e.message}"
                )
            }
        }
    }

    private fun showExportNotification(context: Context, fileUri: Uri, fileName: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Intent để mở file
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Intent để chia sẻ file
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // PendingIntent cho nút mở
            val viewPendingIntent = PendingIntent.getActivity(
                context,
                0,
                viewIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // PendingIntent cho nút chia sẻ
            val sharePendingIntent = PendingIntent.getActivity(
                context,
                1,
                Intent.createChooser(shareIntent, "Chia sẻ báo cáo"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Tạo notification với nhiều action
            val notification = NotificationCompat.Builder(context, "pdf_export")
                .setContentTitle("Xuất báo cáo thành công")
                .setContentText("File: $fileName")
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(viewPendingIntent)
                .addAction(R.drawable.ic_share, "Chia sẻ", sharePendingIntent)
                .build()

            notificationManager.notify(1, notification)
        } catch (e: Exception) {
            Timber.e(e, "Error showing notification")
            _uiState.value = _uiState.value.copy(
                exportStatus = ExportStatus.Error,
                message = "Không thể tạo thông báo: ${e.message}"
            )
        }
    }

    @SuppressLint("ResourceType")
    private fun generatePdfDocument(context: Context, outputStream: OutputStream) {
        try {
            // Tạo font từ resource
            val fontStream = context.resources.openRawResource(R.font.font_1)
            val tempFile = File.createTempFile("temp_font", ".ttf")
            tempFile.deleteOnExit()
            
            fontStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Đăng ký font với iText
            val baseFont = BaseFont.createFont(
                tempFile.absolutePath,
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
            )
            
            val vietnameseFont = Font(baseFont, 12f)
            val titleFont = Font(baseFont, 18f, Font.BOLD)
            val sectionFont = Font(baseFont, 14f, Font.BOLD)

            // Tạo document với margin
            val document = Document(PageSize.A4, 50f, 50f, 50f, 50f)
            PdfWriter.getInstance(document, outputStream)
            document.open()

            // Tiêu đề báo cáo
            val title = Paragraph("DocCarePlus - Báo cáo thống kê", titleFont)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)

            // Ngày xuất báo cáo
            val date = Paragraph("Ngày xuất: ${getCurrentDateTime()}", vietnameseFont)
            date.alignment = Element.ALIGN_RIGHT
            date.spacingAfter = 30f
            document.add(date)

            // Thống kê tổng quan
            val stats = _uiState.value.overviewStats
            if (stats != null) {
                addSection(document, "THỐNG KÊ TỔNG QUAN", sectionFont)
                
                val table = PdfPTable(2)
                table.widthPercentage = 100f
                table.spacingBefore = 10f
                table.spacingAfter = 20f

                addTableRow(table, "Tổng số bác sĩ", "${stats.totalDoctors}", vietnameseFont)
                addTableRow(table, "Tổng số bệnh nhân", "${stats.totalPatients}", vietnameseFont)
                addTableRow(table, "Tổng số cuộc hẹn", "${stats.totalAppointments}", vietnameseFont)
                addTableRow(
                    table, 
                    "Tổng doanh thu", 
                    ChartUtils.formatCurrency(stats.totalRevenue.toFloat()),
                    vietnameseFont
                )
                
                document.add(table)
            }

            // Doanh thu theo tháng
            val revenue = _uiState.value.revenueStats
            if (revenue != null) {
                addSection(document, "DOANH THU THEO THÁNG", sectionFont)
                
                val table = PdfPTable(2)
                table.widthPercentage = 100f
                table.spacingBefore = 10f
                table.spacingAfter = 20f

                revenue.monthlyRevenue.forEach { (month, amount) ->
                    addTableRow(table, "Tháng $month", 
                        ChartUtils.formatCurrency(amount.toFloat()),
                        vietnameseFont)
                }
                
                document.add(table)
            }

            // Thống kê cuộc hẹn
            val appointments = _uiState.value.appointmentStats
            if (appointments != null) {
                addSection(document, "THỐNG KÊ CUỘC HẸN", sectionFont)
                
                val table = PdfPTable(2)
                table.widthPercentage = 100f
                table.spacingBefore = 10f
                table.spacingAfter = 20f

                appointments.appointmentsByDate.forEach { (date, count) ->
                    addTableRow(table, "Ngày $date", "$count cuộc hẹn", vietnameseFont)
                }
                
                document.add(table)
            }

            // Hoạt động gần đây
            val activities = _uiState.value.activities
            if (activities.isNotEmpty()) {
                addSection(document, "HOẠT ĐỘNG GẦN ĐÂY", sectionFont)
                
                activities.forEach { activity ->
                    val activityTitle = Paragraph("${formatDateTime(activity.timestamp)}: ${activity.title}", vietnameseFont)
                    activityTitle.spacingBefore = 10f
                    document.add(activityTitle)
                    
                    val description = Paragraph("    ${activity.description}", vietnameseFont)
                    description.spacingAfter = 10f
                    document.add(description)
                }
            }

            document.close()
            tempFile.delete() // Xóa file font tạm

        } catch (e: Exception) {
            Timber.e(e, "Error generating PDF")
            throw e
        }
    }

    private fun addSection(document: Document, title: String, font: Font) {
        val sectionTitle = Paragraph(title, font)
        sectionTitle.spacingBefore = 20f
        sectionTitle.spacingAfter = 10f
        document.add(sectionTitle)
    }

    private fun addTableRow(table: PdfPTable, label: String, value: String, font: Font) {
        val cell1 = PdfPCell(Paragraph(label, font))
        val cell2 = PdfPCell(Paragraph(value, font))
        
        // Set padding cho từng cạnh
        cell1.setPadding(8f)  // hoặc có thể set riêng:
        cell1.setPaddingLeft(8f)
        cell1.setPaddingRight(8f)
        cell1.setPaddingTop(8f)
        cell1.setPaddingBottom(8f)
        
        cell2.setPadding(8f)  // hoặc có thể set riêng:
        cell2.setPaddingLeft(8f)
        cell2.setPaddingRight(8f)
        cell2.setPaddingTop(8f)
        cell2.setPaddingBottom(8f)
        
        cell1.borderColor = BaseColor.LIGHT_GRAY
        cell2.borderColor = BaseColor.LIGHT_GRAY
        
        table.addCell(cell1)
        table.addCell(cell2)
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault()).format(Date())
    }

    private fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}