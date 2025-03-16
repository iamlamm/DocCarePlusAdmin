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

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var activityAdapter: ActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupNotificationButton()
        observeNotifications()

        // Log để debug
        Timber.d("DashboardFragment: onViewCreated")
    }
    
    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter()
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activityAdapter
            setHasFixedSize(true)
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
            // Implement loading indicators as needed
            // For example:
            rvRecentActivity.isVisible = !isLoading
            // Add more loading indicators for other views
        }
    }
    
    private fun updateDashboardData(state: DashboardState) {
        binding.apply {
            // Format currency với USD
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            
            // Update doctor stats
            tvDoctorCount.text = state.doctorsCount.toString()
//            tvPendingDoctors.apply {
//                text = if (state.pendingDoctorsCount > 0) {
//                    "${state.pendingDoctorsCount} chờ duyệt"
//                } else {
//                    "Không có bác sĩ chờ duyệt"
//                }
//                isVisible = state.pendingDoctorsCount > 0
//            }
            
            // Update user stats
            tvUserCount.text = state.usersCount.toString()
            
            // Update appointments
            tvAppointmentCount.text = state.todayAppointments.toString()
            
            // Update revenue - sử dụng định dạng USD
            tvRevenue.text = currencyFormat.format(state.monthlyRevenue)
            
            // Update recent activities
            Timber.d("Updating recent activities: ${state.recentActivities.size} items")
            activityAdapter.submitList(state.recentActivities)
            rvRecentActivity.isVisible = state.recentActivities.isNotEmpty()
        }
    }
    
    private fun setupClickListeners() {
        // Setup notification button click
        binding.btnNotification.setOnClickListener {
            // Navigate to notifications
        }
        
        // Setup quick action clicks
        binding.actionApproveDoctors.setOnClickListener {
            // Navigate to approve doctors screen
        }
        
        binding.actionAddCategory.setOnClickListener {
            // Navigate to add category screen
        }
        
        binding.actionViewReports.setOnClickListener {
            // Navigate to reports screen
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}