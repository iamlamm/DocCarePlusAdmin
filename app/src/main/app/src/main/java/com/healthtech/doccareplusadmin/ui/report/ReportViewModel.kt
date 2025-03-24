package com.healthtech.doccareplusadmin.ui.report

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ReportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportState())
    val uiState: StateFlow<ReportState> = _uiState

    private fun exportReport(context: Context) {
        viewModelScope.launch {
            try {
                val timeStamp = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault()).format(Date())
                val fileName = "DocCarePlus_Report_$timeStamp.pdf"

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    if (Build.VERSION.SDK_INT >= Build.VERSION.SDK_INT_Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let { documentUri ->
                    resolver.openOutputStream(documentUri)?.use { outputStream ->
                        // Tạo document PDF
                        val document = Document()
                        PdfWriter.getInstance(document, outputStream)
                        document.open()
                        
                        // Thêm nội dung vào PDF
                        document.add(Paragraph("Báo cáo DocCarePlus"))
                        // ... thêm các nội dung khác của báo cáo
                        
                        document.close()
                        
                        // Cập nhật UI state
                        _uiState.update { currentState ->
                            currentState.copy(
                                exportStatus = ExportStatus.Success(documentUri),
                                message = "Xuất báo cáo thành công"
                            )
                        }
                    }
                } ?: throw IOException("Không thể tạo file")

            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        exportStatus = ExportStatus.Error,
                        message = "Lỗi khi xuất báo cáo: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}