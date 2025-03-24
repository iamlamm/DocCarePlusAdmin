package com.healthtech.doccareplusadmin.ui.report

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
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
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
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
import com.healthtech.doccareplusadmin.ui.report.chart.ChartUtils.formatCurrency
import com.healthtech.doccareplusadmin.ui.report.chart.PieChartConfig
import com.healthtech.doccareplusadmin.ui.report.chart.applyConfig
import com.healthtech.doccareplusadmin.utils.PermissionManager
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showWarningDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels()
    private lateinit var activityAdapter: ActivityAdapter

    private var userDistributionChart: PieChart? = null
    private var appointmentStatusChart: PieChart? = null
    private var revenueChart: LineChart? = null
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
        
        // Khởi tạo các biến chart
        userDistributionChart = binding.userDistributionChart
        appointmentStatusChart = binding.appointmentStatusChart
        revenueChart = binding.revenueChart
        appointmentsChart = binding.appointmentsChart
        
        initializeCharts()
        setupTabLayout()
        setupDateRangePicker()
        setupRecyclerView()
        setupExportButton()
        observeData()
        handleExportReport()
        loadInitialData()
    }

    private fun initializeCharts() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val pieConfig = PieChartConfig(
                holeEnabled = true,
                centerText = true,
                rotationEnabled = true,
                highlightPerTapEnabled = true,
                entryLabels = false,
                legend = true
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

            withContext(Dispatchers.Main) {
                // Cấu hình cho userDistributionChart
                userDistributionChart?.apply {
                    pieConfig.applyTo(this)
                }

                // Cấu hình cho appointmentStatusChart
                appointmentStatusChart?.apply {
                    pieConfig.applyTo(this)
                }

                // Cấu hình cho revenueChart
                revenueChart?.apply {
                    applyConfig(revenueConfig)
                }

                // Cấu hình cho appointmentsChart
                appointmentsChart?.apply {
                    pieConfig.applyTo(this)
                }
            }
        }
    }

    private fun updateOverviewData(stats: OverviewStats) {
        // Cấu hình biểu đồ phân bố người dùng
        val userEntries = listOf(
            PieEntry(stats.totalDoctors.toFloat(), "Bác sĩ"),
            PieEntry(stats.totalPatients.toFloat(), "Bệnh nhân")
        )

        userDistributionChart?.let {
            setupPieChart(
                it,
                userEntries,
                "Phân bố người dùng",
                listOf(
                    ContextCompat.getColor(requireContext(), R.color.doctor_color),
                    ContextCompat.getColor(requireContext(), R.color.patient_color)
                )
            )
        }

        // Cấu hình biểu đồ trạng thái cuộc hẹn
        val appointmentEntries = listOf(
            PieEntry(stats.pendingAppointments.toFloat(), "Chờ khám"),
            PieEntry(stats.completedAppointments.toFloat(), "Đã khám"),
            PieEntry(stats.cancelledAppointments.toFloat(), "Đã hủy")
        )

        appointmentStatusChart?.let {
            setupPieChart(
                it,
                appointmentEntries,
                "Trạng thái cuộc hẹn",
                listOf(
                    ContextCompat.getColor(requireContext(), R.color.pending_color),
                    ContextCompat.getColor(requireContext(), R.color.completed_color),
                    ContextCompat.getColor(requireContext(), R.color.cancelled_color)
                )
            )
        }
    }

    private fun setupPieChart(
        chart: PieChart,
        entries: List<PieEntry>,
        label: String,
        colors: List<Int>
    ) {
        val dataSet = PieDataSet(entries, label).apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(chart)
        }

        chart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textSize = 12f
            }
            centerText = label
            setCenterTextSize(14f)
            animateY(1000)
            invalidate()
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
        try {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Chọn khoảng thời gian")
                .setSelection(
                    androidx.core.util.Pair(
                        viewModel.selectedDateRange.value?.first ?: MaterialDatePicker.todayInUtcMilliseconds(),
                        viewModel.selectedDateRange.value?.second ?: MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .setTheme(R.style.ThemeMaterialCalendar)
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                try {
                    val startDate = selection.first
                    val endDate = selection.second
                    
                    if (startDate > endDate) {
                        showError("Ngày bắt đầu không thể sau ngày kết thúc")
                        return@addOnPositiveButtonClickListener
                    }

                    viewModel.updateDateRange(startDate, endDate)
                    updateDateRangeText(startDate, endDate)
                } catch (e: Exception) {
                    handleError(e, "Lỗi cập nhật khoảng thời gian")
                }
            }

            dateRangePicker.show(childFragmentManager, "date_range_picker")
        } catch (e: Exception) {
            handleError(e, "Lỗi hiển thị date picker")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateRangeText(startDate: Long, endDate: Long) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.dateRangeInput.setText(
                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
            )
        } catch (e: Exception) {
            handleError(e, "Lỗi cập nhật text ngày tháng")
        }
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
            // Show/hide loading indicators
            val loadingViews = listOf(
                overviewLoadingProgress,
                revenueLoadingProgress,
                appointmentsLoadingProgress,
                activitiesLoadingProgress
            )
            
            loadingViews.forEach { it.isVisible = isLoading }

            // Show/hide content based on current tab
            if (!isLoading) {
                updateVisibility(viewModel.selectedTab.value)
            }
            
            // Disable interaction while loading
            tabLayout.isEnabled = !isLoading
            dateRangeInput.isEnabled = !isLoading
            btnExport.isEnabled = !isLoading
        }
    }

    private fun updateCharts(state: ReportState) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                binding.apply {
                    // Update Overview section với 2 biểu đồ tròn
                    if (state.overviewStats != null) {
                        userDistributionChart.isVisible = true
                        appointmentStatusChart.isVisible = true
                        overviewEmptyView.isVisible = false
                        updateOverviewData(state.overviewStats)
                    } else {
                        userDistributionChart.isVisible = false
                        appointmentStatusChart.isVisible = false
                        overviewEmptyView.isVisible = true
                    }

                    // Update Revenue
                    revenueChart.isVisible = state.revenueStats != null
                    revenueEmptyView.isVisible = state.revenueStats == null
                    state.revenueStats?.let { updateRevenueData(it) }

                    // Update Appointments
                    appointmentsChart.isVisible = state.appointmentStats != null
                    appointmentsEmptyView.isVisible = state.appointmentStats == null
                    state.appointmentStats?.let { prepareAppointmentData(it) }?.let { 
                        updateChart(appointmentsChart, it) 
                    }

                    // Update Activities
                    activitiesEmptyView.isVisible = state.activities.isEmpty()
                    activitiesRecyclerView.isVisible = state.activities.isNotEmpty()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
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
        view?.let { view ->
            Snackbar.make(view, error, Snackbar.LENGTH_LONG)
                .setAction("Thử lại") {
                    loadInitialData()
                }
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                .show()
        }
    }

    private fun handleError(e: Exception, message: String) {
        Timber.e(e, message)
        showError("$message: ${e.localizedMessage}")
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

    @SuppressLint("SetTextI18n")
    private fun updateRevenueData(stats: RevenueStats) {
        try {
            // Chuyển đổi dữ liệu từ MonthlyRevenueData sang Entry cho biểu đồ
            val entries = stats.monthlyRevenue.entries
                .sortedBy { it.key }
                .mapIndexed { index, entry ->
                    Entry(index.toFloat(), entry.value.totalAmount.toFloat())
                }

            val dataSet = LineDataSet(entries, "Doanh thu").apply {
                // Gradient colors
                val gradientColors = listOf(
                    ContextCompat.getColor(requireContext(), R.color.revenue_gradient_start),
                    ContextCompat.getColor(requireContext(), R.color.revenue_gradient_end)
                )
                setGradientColor(gradientColors[0], gradientColors[1])
                
                // Line styling
                color = ContextCompat.getColor(requireContext(), R.color.revenue_line_color)
                lineWidth = 2.5f
                
                // Point styling
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.revenue_point_color))
                circleRadius = 5f
                circleHoleRadius = 2.5f
                
                // Value formatting
                valueTextSize = 11f
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.revenue_text_color)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrencyCompact(value.toLong())
                    }
                }
                
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillAlpha = 50
            }

            // Thêm dataset phụ cho số lượng cuộc hẹn
            val appointmentEntries = stats.monthlyRevenue.entries
                .sortedBy { it.key }
                .mapIndexed { index, entry ->
                    Entry(index.toFloat(), entry.value.appointmentsCount.toFloat())
                }

            val appointmentDataSet = LineDataSet(appointmentEntries, "Số cuộc hẹn").apply {
                color = ContextCompat.getColor(requireContext(), R.color.appointment_line_color)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.appointment_point_color))
                lineWidth = 2f
                circleRadius = 4f
                valueTextSize = 10f
                axisDependency = YAxis.AxisDependency.RIGHT
            }

            revenueChart?.apply {
                clear()
                
                // Set multiple datasets
                data = LineData(listOf(dataSet, appointmentDataSet))
                
                // X-axis configuration
                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(
                        stats.monthlyRevenue.keys.map { formatMonthLabel(it) }
                    )
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textColor = ContextCompat.getColor(requireContext(), R.color.axis_text_color)
                    textSize = 10f
                }

                // Left Y-axis (Revenue)
                axisLeft.apply {
                    val maxRevenue = stats.monthlyRevenue.values.maxOfOrNull { it.totalAmount } ?: 0.0
                    axisMaximum = (maxRevenue * 1.2).toFloat()
                    axisMinimum = 0f
                    setDrawGridLines(true)
                    gridColor = ContextCompat.getColor(requireContext(), R.color.grid_color)
                    gridLineWidth = 0.5f
                    textColor = ContextCompat.getColor(requireContext(), R.color.axis_text_color)
                    textSize = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatCurrencyCompact(value.toLong())
                        }
                    }
                }

                // Right Y-axis (Appointments)
                axisRight.apply {
                    isEnabled = true
                    val maxAppointments = stats.monthlyRevenue.values.maxOfOrNull { it.appointmentsCount } ?: 0
                    axisMaximum = (maxAppointments * 1.2).toFloat()
                    axisMinimum = 0f
                    textColor = ContextCompat.getColor(requireContext(), R.color.appointment_text_color)
                    textSize = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }
                }

                // Legend configuration
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    form = Legend.LegendForm.LINE
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }

                // Interaction
                setTouchEnabled(true)
                setPinchZoom(true)
                setScaleEnabled(true)
                
                // Animation
                animateXY(1000, 1000)
                
                invalidate()
            }

            // Hiển thị thông tin tổng quan
            binding.apply {
                val totalRevenue = stats.monthlyRevenue.values.sumOf { it.totalAmount }
                val totalAppointments = stats.monthlyRevenue.values.sumOf { it.appointmentsCount }
                val averageRevenue = if (totalAppointments > 0) {
                    totalRevenue / totalAppointments
                } else 0.0

                tvTotalRevenue.text = formatCurrencyCompact(totalRevenue.toLong())
                tvTotalAppointments.text = "$totalAppointments appointments"
                tvAverageRevenue.text = "Avg: ${formatCurrencyCompact(averageRevenue.toLong())}/appointment"
            }

        } catch (e: Exception) {
            Timber.e(e, "Error updating revenue chart")
            showError("Lỗi cập nhật biểu đồ doanh thu")
        }
    }

    private fun formatMonthLabel(monthKey: String): String {
        return try {
            // Chuyển đổi format từ "2025-03" thành "T3/2025"
            val parts = monthKey.split("-")
            "T${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            monthKey
        }
    }

    // Hàm hỗ trợ format tiền tệ dạng rút gọn
    private fun formatCurrencyCompact(amount: Long): String {
        return "$" + when {
            amount >= 1_000_000_000 -> "%.1fB".format(amount / 1_000_000_000.0)
            amount >= 1_000_000 -> "%.1fM".format(amount / 1_000_000.0)
            amount >= 1_000 -> "%.1fK".format(amount / 1_000.0)
            else -> amount.toString()
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
        userDistributionChart = null
        appointmentStatusChart = null
        revenueChart = null
        appointmentsChart = null
        _binding = null
    }

    @Deprecated("Deprecated in Java")
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

    @SuppressLint("SetTextI18n")
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