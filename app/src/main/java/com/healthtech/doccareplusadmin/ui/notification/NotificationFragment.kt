package com.healthtech.doccareplusadmin.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.healthtech.doccareplusadmin.common.base.BaseFragment
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.databinding.FragmentNotificationBinding
import com.healthtech.doccareplusadmin.ui.notification.adapter.NotificationAdapter
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationFragment : BaseFragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by viewModels()
    private val adapter = NotificationAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUiState()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter.setOnNotificationClickListener { notificationId ->
            viewModel.markAsRead(notificationId)
        }

        binding.rcvNotification.apply {
            this.adapter = this@NotificationFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBarNotification.setLoading(true)
                        binding.tvEmpty.visibility = View.GONE
                    }

                    is UiState.Success -> {
                        binding.progressBarNotification.setLoading(false)
                        binding.tvEmpty.visibility =
                            if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        adapter.submitList(state.data)
                    }

                    is UiState.Error -> {
                        binding.progressBarNotification.setLoading(false)
                        binding.tvEmpty.visibility = View.VISIBLE
                        SnackbarUtils.showErrorSnackbar(
                            binding.root,
                            state.message
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun cleanupViewReferences() {
        _binding = null
        super.cleanupViewReferences()
    }
}