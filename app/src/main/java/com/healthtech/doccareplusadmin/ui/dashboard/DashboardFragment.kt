package com.healthtech.doccareplusadmin.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.FragmentDashboardBinding
import com.healthtech.doccareplusadmin.ui.dashboard.adapter.ActivityAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import timber.log.Timber
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.core.content.ContextCompat
import com.healthtech.doccareplusadmin.domain.model.AppointmentsStats
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var activityAdapter: ActivityAdapter
    private var chart: LineChart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("DashboardFragment.onViewCreated")
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupNotificationButton()
        observeNotifications()
        setupAppointmentsChart()
        
        // Thêm lifecycle observer
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                Timber.d("DashboardFragment.onResume")
                viewModel.refreshData()
            }
        })
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter()
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activityAdapter
            setHasFixedSize(true)
            itemAnimator = null // Tắt animation để tránh lỗi hiển thị
        }
    }

    private fun setupObservers() {
        viewModel.currentDate.observe(viewLifecycleOwner) { date ->
            binding.tvDate.text = date
        }

        viewModel.dashboardState.observe(viewLifecycleOwner) { state ->
            Timber.d("DashboardState updated: $state")

            // Update loading state
            updateLoadingState(state.isLoading)

            // Update dashboard data
            if (!state.isLoading) {
                updateDashboardData(state)
            }

            // Show error if any
            state.error?.let { error ->
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            // Show/hide loading indicators
            progressBar.isVisible = isLoading
            
            // Show/hide content based on loading state
            contentLayout.isVisible = !isLoading
            
            // Keep recent activities visible if they exist
            val hasActivities = !viewModel.dashboardState.value?.recentActivities.isNullOrEmpty()
            rvRecentActivity.isVisible = hasActivities && !isLoading
            tvNoActivities.isVisible = !hasActivities && !isLoading
        }
    }

    private fun updateDashboardData(state: DashboardState) {
        binding.apply {
            // Update UI only if not loading
            if (!state.isLoading) {
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                tvDoctorCount.text = state.doctorsCount.toString()
                tvUserCount.text = state.usersCount.toString()
                tvAppointmentCount.text = state.todayAppointments.toString()
                tvRevenue.text = currencyFormat.format(state.monthlyRevenue)

                // Update recent activities
                val activities = state.recentActivities
                Timber.d("Updating recent activities: ${activities.size} items")
                
                // Show/hide activities related views
                val hasActivities = activities.isNotEmpty()
                rvRecentActivity.isVisible = hasActivities
                tvRecentActivity.isVisible = true
                tvNoActivities.isVisible = !hasActivities

                if (hasActivities) {
                    activityAdapter.submitList(activities) {
                        // Callback sau khi list được cập nhật
                        rvRecentActivity.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Setup notification button click
            btnNotification.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_notification)
            }

            // Setup quick action clicks
            actionManageDoctors.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_allDoctors)
            }

            actionManageCategories.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_allCategories)
            }

            actionViewReports.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_report)
            }
        }
    }

    private fun setupNotificationButton() {
        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_notification)
        }
    }

    private fun observeNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.unreadNotificationsCount.collect { count ->
                updateNotificationBadge(count)
            }
        }
    }

    private fun updateNotificationBadge(count: Int) {
        binding.tvNotificationBadge.apply {
            visibility = if (count > 0) View.VISIBLE else View.GONE
            text = if (count > 99) "99+" else count.toString()
        }
    }

    private fun setupAppointmentsChart() {
        chart = LineChart(requireContext()).apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
            }

            axisRight.isEnabled = false
            animateX(1000)
        }

        binding.chartContainer.addView(chart)

        // Observe dashboard state for appointments stats
        viewModel.dashboardState.observe(viewLifecycleOwner) { state ->
            state.appointmentsStats?.let { stats ->
                updateChart(stats)
            }
        }
    }

    private fun updateChart(stats: AppointmentsStats) {
        val entries = stats.appointmentsByDate.entries
            .sortedBy { it.key }
            .mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.value.toFloat())
            }

        val dataSet = LineDataSet(entries, "Cuộc hẹn").apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_line_color)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.chart_line_color))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.chart_fill_color)
            fillAlpha = 100
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        chart?.apply {
            data = LineData(dataSet)
            
            // Cập nhật labels trục X
            xAxis.valueFormatter = IndexAxisValueFormatter(
                stats.appointmentsByDate.keys
                    .map { formatDateLabel(it) }
                    .toList()
            )

            // Cập nhật giới hạn trục Y
            axisLeft.apply {
                val maxValue = stats.appointmentsByDate.values.maxOrNull() ?: 0
                axisMaximum = (maxValue + 2).toFloat() // Thêm khoảng trống phía trên
            }

            // Refresh biểu đồ
            invalidate()
        }
    }

    private fun formatDateLabel(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            "${parts[2]}/${parts[1]}"
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chart = null
        _binding = null
    }

    companion object {
        private const val MAX_VISIBLE_ACTIVITIES = 5
    }
}