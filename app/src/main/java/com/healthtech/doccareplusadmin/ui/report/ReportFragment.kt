package com.healthtech.doccareplusadmin.ui.report

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.FragmentReportBinding
import com.healthtech.doccareplusadmin.domain.model.Activity
import com.healthtech.doccareplusadmin.domain.model.AppointmentStats
import com.healthtech.doccareplusadmin.domain.model.OverviewStats
import com.healthtech.doccareplusadmin.domain.model.RevenueStats
import com.healthtech.doccareplusadmin.ui.dashboard.adapter.ActivityAdapter
import com.healthtech.doccareplusadmin.ui.report.chart.ChartConfig
import com.healthtech.doccareplusadmin.ui.report.chart.ChartUtils
import com.healthtech.doccareplusadmin.ui.report.chart.PieChartConfig
import com.healthtech.doccareplusadmin.ui.report.chart.applyConfig
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showWarningDialog
import com.healthtech.doccareplusadmin.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import timber.log.Timber
import androidx.annotation.RequiresApi
import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import com.google.android.material.snackbar.Snackbar
import com.github.mikephil.charting.components.AxisBase

@AndroidEntryPoint
class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels()
    private lateinit var activityAdapter: ActivityAdapter

    private var overviewChart: LineChart? = null
    private var revenueChart: BarChart? = null
    private var appointmentsChart: PieChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tạo notification channel
        PermissionManager.createNotificationChannel(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeCharts()
        setupTabLayout()
        setupDateRangePicker()
        setupRecyclerView()
        setupExportButton()
        observeData()
        handleExportReport()
        
        // Thêm dòng này để load dữ liệu ban đầu
        loadInitialData()
    }

    private fun initializeCharts() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val overviewConfig = ChartConfig(
                shouldShowDescription = false,
                shouldEnableTouch = true,
                shouldEnableDrag = true,
                shouldEnableScale = true,
                shouldEnablePinchZoom = true,
                shouldShowGrid = false,
                shouldShowRightAxis = false,
                xAxisPosition = XAxis.XAxisPosition.BOTTOM
            )

            val revenueConfig = ChartConfig(
                shouldShowDescription = false,
                shouldEnableTouch = true,
                shouldEnableDrag = true,
                shouldEnableScale = true,
                shouldEnablePinchZoom = false,
                shouldShowGrid = false,
                shouldShowRightAxis = false,
                xAxisPosition = XAxis.XAxisPosition.BOTTOM
            )

            val pieConfig = PieChartConfig(
                holeEnabled = true,
                centerText = true,
                rotationEnabled = true,
                highlightPerTapEnabled = true,
                entryLabels = false,
                legend = true
            )

            withContext(Dispatchers.Main) {
                binding.overviewChart.apply {
                    applyConfig(overviewConfig)
                }
                binding.revenueChart.apply {
                    applyConfig(revenueConfig)
                }
                binding.appointmentsChart.apply {
                    pieConfig.applyTo(this)
                }
            }
        }
    }

    private fun setupOverviewChart() {
        overviewChart?.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
        }
    }

    private fun setupRevenueChart() {
        revenueChart?.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
        }
    }

    private fun setupAppointmentsChart() {
        appointmentsChart?.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setDrawEntryLabels(false)
            legend.isEnabled = true
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    viewModel.updateSelectedTab(position)
                    updateVisibility(position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupDateRangePicker() {
        binding.dateRangeInput.setOnClickListener {
            showDateRangePicker()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Chọn khoảng thời gian")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second
            viewModel.updateDateRange(startDate, endDate)

            // Update input text
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.dateRangeInput.setText(
                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
            )
        }

        dateRangePicker.show(childFragmentManager, "date_range_picker")
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter()
        binding.activitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activityAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupExportButton() {
        binding.btnExport.setOnClickListener {
            if (!checkPermissions()) {
                return@setOnClickListener
            }

            showWarningDialog(
                title = "Xuất báo cáo",
                message = "Bạn có chắc chắn muốn xuất báo cáo này?",
                onPositive = {
                    context?.let { ctx ->
                        viewModel.exportReport(ctx)
                    }
                }
            )
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = PermissionManager.STORAGE_PERMISSIONS
        return if (!PermissionManager.hasPermissions(requireContext(), permissions)) {
            PermissionManager.requestPermissions(requireActivity(), permissions)
            false
        } else {
            true
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateLoadingState(state.isLoading)
                updateCharts(state)
                updateActivities(state.activities)

                state.error?.let { error ->
                    showError(error)
                }
            }
        }
    }

    private fun updateVisibility(tabPosition: Int) {
        binding.apply {
            overviewCard.isVisible = tabPosition == 0
            revenueCard.isVisible = tabPosition == 1
            appointmentsCard.isVisible = tabPosition == 2
            activitiesCard.isVisible = tabPosition == 3
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            overviewLoadingProgress.isVisible = isLoading
            revenueLoadingProgress.isVisible = isLoading
            appointmentsLoadingProgress.isVisible = isLoading
            activitiesLoadingProgress.isVisible = isLoading

            if (isLoading) {
                overviewChart.isVisible = false
                revenueChart.isVisible = false
                appointmentsChart.isVisible = false
                activitiesRecyclerView.isVisible = false
            } else {
                updateVisibility(viewModel.selectedTab.value)
            }
        }
    }

    private fun updateCharts(state: ReportState) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val overviewData = prepareOverviewData(state.overviewStats)
            val revenueData = prepareRevenueData(state.revenueStats)
            val appointmentData = prepareAppointmentData(state.appointmentStats)

            withContext(Dispatchers.Main) {
                binding.apply {
                    // Update Overview
                    overviewChart.isVisible = overviewData != null
                    overviewEmptyView.isVisible = overviewData == null
                    overviewData?.let { updateChart(overviewChart, it) }

                    // Update Revenue
                    revenueChart.isVisible = revenueData != null
                    revenueEmptyView.isVisible = revenueData == null
                    revenueData?.let { updateChart(revenueChart, it) }

                    // Update Appointments
                    appointmentsChart.isVisible = appointmentData != null
                    appointmentsEmptyView.isVisible = appointmentData == null
                    appointmentData?.let { updateChart(appointmentsChart, it) }

                    // Update Activities
                    activitiesEmptyView.isVisible = state.activities.isEmpty()
                    activitiesRecyclerView.isVisible = state.activities.isNotEmpty()
                }
            }
        }
    }

    private fun updateActivities(activities: List<Activity>) {
        binding.apply {
            if (activities.isEmpty()) {
                activitiesEmptyView.isVisible = true
                activitiesRecyclerView.isVisible = false
                activitiesEmptyView.text = "Không có hoạt động nào trong khoảng thời gian này"
            } else {
                activitiesEmptyView.isVisible = false
                activitiesRecyclerView.isVisible = true
                activityAdapter.submitList(activities)
                Timber.d("Updated activities: ${activities.size} items")
            }
        }
    }

    private fun showError(error: String) {
        SnackbarUtils.showErrorSnackbar(
            view = binding.root,
            message = error
        )
    }

    private fun updateChart(chart: Chart<*>?, data: Any?) {
        try {
            when {
                chart is LineChart && data is LineData -> {
                    chart.apply {
                        this.data = data
                        notifyDataSetChanged()
                        invalidate()
                    }
                }

                chart is BarChart && data is BarData -> {
                    chart.apply {
                        this.data = data
                        notifyDataSetChanged()
                        invalidate()
                    }
                }

                chart is PieChart && data is PieData -> {
                    chart.apply {
                        this.data = data
                        notifyDataSetChanged()
                        invalidate()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating chart")
            showError("Lỗi cập nhật biểu đồ")
        }
    }

    private fun prepareOverviewData(stats: OverviewStats?): BarData? {
        return stats?.let {
            try {
                val entries = listOf(
                    BarEntry(0f, it.totalDoctors.toFloat()),
                    BarEntry(1f, it.totalPatients.toFloat()),
                    BarEntry(2f, it.totalAppointments.toFloat()),
                    BarEntry(3f, it.pendingAppointments.toFloat()),
                    BarEntry(4f, it.completedAppointments.toFloat()),
                    BarEntry(5f, it.cancelledAppointments.toFloat())
                )

                if (entries.all { entry -> entry.y == 0f }) {
                    return null
                }

                val dataSet = BarDataSet(entries, "Thống kê").apply {
                    colors = listOf(
                        ContextCompat.getColor(requireContext(), R.color.doctor_color),
                        ContextCompat.getColor(requireContext(), R.color.patient_color),
                        ContextCompat.getColor(requireContext(), R.color.appointment_color),
                        ContextCompat.getColor(requireContext(), R.color.pending_color),
                        ContextCompat.getColor(requireContext(), R.color.completed_color),
                        ContextCompat.getColor(requireContext(), R.color.cancelled_color)
                    )
                    valueTextSize = 12f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }
                }

                BarData(dataSet).apply {
                    barWidth = 0.7f
                    // Thêm labels cho trục X
                    setValueFormatter(object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase): String {
                            return when (value.toInt()) {
                                0 -> "Bác sĩ"
                                1 -> "Bệnh nhân"
                                2 -> "Tổng cuộc hẹn"
                                3 -> "Chờ khám"
                                4 -> "Đã khám"
                                5 -> "Đã hủy"
                                else -> ""
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                Timber.e(e, "Error preparing overview data")
                null
            }
        }
    }

    private fun prepareRevenueData(stats: RevenueStats?): BarData? {
        return stats?.let {
            val entries = it.monthlyRevenue.entries
                .sortedBy { entry -> entry.key }
                .mapIndexed { index, entry ->
                    BarEntry(index.toFloat(), entry.value.toFloat())
                }

            if (entries.isEmpty() || entries.all { entry -> entry.y == 0f }) {
                return null
            }

            val dataSet = BarDataSet(entries, "Doanh thu theo tháng").apply {
                color = ContextCompat.getColor(requireContext(), R.color.chart_bar_color)
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return ChartUtils.formatCurrency(value)
                    }
                }
            }

            BarData(dataSet).apply {
                barWidth = 0.7f
            }
        }
    }

    private fun prepareAppointmentData(stats: AppointmentStats?): PieData? {
        return stats?.let {
            val entries = it.appointmentsByDate.entries
                .sortedBy { entry -> entry.key }
                .map { entry ->
                    PieEntry(entry.value.toFloat(), entry.key)
                }

            if (entries.isEmpty() || entries.all { entry -> entry.value == 0f }) {
                return null
            }

            val dataSet = PieDataSet(entries, "Cuộc hẹn theo ngày").apply {
                colors = ChartUtils.getChartColors(requireContext())
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return ChartUtils.formatValue(value)
                    }
                }
                valueTextColor = Color.WHITE
                sliceSpace = 2f
            }

            PieData(dataSet)
        }
    }

    private fun handleExportReport() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state.exportStatus) {
                    is ExportStatus.Success -> {
                        val uri = (state.exportStatus as ExportStatus.Success).uri
                        
                        // Hiển thị dialog với nhiều tùy chọn
                        context?.let { ctx ->
                            val options = arrayOf("Mở file", "Chia sẻ", "Xem vị trí file")
                            androidx.appcompat.app.AlertDialog.Builder(ctx)
                                .setTitle("Xuất báo cáo thành công")
                                .setItems(options) { _, which ->
                                    when (which) {
                                        0 -> openPdfFile(uri)
                                        1 -> sharePdfFile(uri)
                                        2 -> showFileLocation(state.message)
                                    }
                                }
                                .show()
                        }
                    }
                    is ExportStatus.Error -> {
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> { /* Không làm gì */ }
                }
            }
        }
    }

    private fun openPdfFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(uri, "application/pdf")
        }
        try {
            startActivity(Intent.createChooser(intent, "Chọn ứng dụng để mở PDF"))
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, "Không tìm thấy ứng dụng để mở PDF", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun sharePdfFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            startActivity(Intent.createChooser(intent, "Chia sẻ báo cáo"))
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Không thể chia sẻ file", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showFileLocation(message: String) {
        context?.let { ctx ->
            androidx.appcompat.app.AlertDialog.Builder(ctx)
                .setTitle("Vị trí file")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        overviewChart = null
        revenueChart = null
        appointmentsChart = null
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionManager.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    setupExportButton()
                } else {
                    showError("Cần cấp quyền truy cập để xuất báo cáo")
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadInitialData() {
        // Set default date range to last 30 days
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (30 * 24 * 60 * 60 * 1000L) // 30 days
        
        // Cập nhật text hiển thị date range
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateRangeInput.setText(
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        )
        
        // Load data
        viewModel.updateDateRange(startDate, endDate)
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE_ANDROID_14 = 101
    }
}