package com.healthtech.doccareplusadmin.ui.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.common.base.BaseFragment
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.databinding.FragmentAllUsersBinding
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.ui.user.adapter.AllUserAdapter
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showWarningDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import timber.log.Timber

@AndroidEntryPoint
class AllUsersFragment : BaseFragment() {
    private var _binding: FragmentAllUsersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllUsersViewModel by viewModels()
    private lateinit var userAdapter: AllUserAdapter
    private var isSearchVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupAdapter()
        setupRecyclerView()
        setupSearchView()
        setupFab()
        setupBackPressHandler()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = "Manage Users"
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_search -> {
                        toggleSearchView()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupAdapter() {
        userAdapter = AllUserAdapter().apply {
            setOnUserClickListener { user ->
                navigateToEditUser(user)
            }
            setOnEditClickListener { user ->
                navigateToEditUser(user)
            }
            setOnDeleteClickListener { user ->
                showDeleteConfirmationDialog(user)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rcvAllUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.setSearchQuery(newText ?: "")
                    return true
                }
            })
        }
    }

    private fun setupFab() {
        binding.fabAddUser.setOnClickListener {
            navigateToAddUser()
        }
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSearchVisible) {
                    toggleSearchView()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun observeViewModel() {
        // Observe all users state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.usersState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBarAllUsers.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBarAllUsers.visibility = View.GONE
                        binding.tvEmptyState.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        
                        // Thêm log để debug
                        Timber.d("AllUsersFragment: Received ${state.data.size} users from ViewModel")
                        
                        // Kiểm tra trùng lặp
                        val userIds = state.data.map { it.id }
                        val duplicateIds = userIds.groupBy { it }.filter { it.value.size > 1 }.keys
                        if (duplicateIds.isNotEmpty()) {
                            Timber.w("Phát hiện ID trùng lặp trong ViewModel data: $duplicateIds")
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBarAllUsers.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.tvEmptyState.text = state.message
                        Timber.e("Error loading users: ${state.message}")
                    }
                    else -> {
                        // Do nothing for idle state
                    }
                }
            }
        }

        // Observe filtered users
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredUsers.collect { users ->
                binding.tvEmptyState.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
                binding.rcvAllUsers.visibility = if (users.isEmpty()) View.GONE else View.VISIBLE
                
                // Thêm log để debug
                Timber.d("AllUsersFragment: Setting ${users.size} users to adapter")
                
                // Đơn giản hóa việc set users
                userAdapter.submitList(users)
            }
        }

        // Observe delete state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Show loading indicator if needed
                    }
                    is UiState.Success -> {
                        SnackbarUtils.showSuccessSnackbar(
                            binding.root,
                            "User deleted successfully"
                        )
                    }
                    is UiState.Error -> {
                        Timber.e("Error deleting user: ${state.message}")
                        // Nếu lỗi là "User not found", hiển thị thông báo khác
                        if (state.message.contains("not found", ignoreCase = true)) {
                            SnackbarUtils.showWarningSnackbar(
                                binding.root,
                                "User has already been deleted or does not exist"
                            )
                            // Làm mới danh sách
//                            viewModel.refreshUsers()
                        } else {
                            SnackbarUtils.showErrorSnackbar(
                                binding.root,
                                state.message
                            )
                        }
                    }
                    else -> {
                        // Do nothing for idle state
                    }
                }
            }
        }

        // Observe search query
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                binding.searchView.setQuery(query, false)
            }
        }
    }

    private fun toggleSearchView() {
        isSearchVisible = !isSearchVisible
        if (isSearchVisible) {
            binding.searchView.visibility = View.VISIBLE
            binding.searchView.requestFocus()

            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)

            binding.toolbar.title = ""
        } else {
            binding.searchView.visibility = View.GONE
            binding.toolbar.title = "Manage Users"
        }
    }

    private fun navigateToEditUser(user: User) {
        findNavController().navigate(
            AllUsersFragmentDirections.actionAllUsersToEditUser(user.id)
        )
    }

    private fun navigateToAddUser() {
        findNavController().navigate(
            AllUsersFragmentDirections.actionAllUsersToEditUser(null)
        )
    }

    private fun showDeleteConfirmationDialog(user: User) {
        showWarningDialog(
            title = "Delete User",
            message = "Are you sure you want to delete ${user.name}?",
            positiveText = "Delete",
            negativeText = "Cancel",
            onPositive = {
                viewModel.deleteUser(user.id)
            }
        )
    }

    override fun onResume() {
        super.onResume()

        // Update search view visibility
        if (isSearchVisible) {
            binding.searchView.visibility = View.VISIBLE
            binding.toolbar.title = ""
        } else {
            binding.searchView.visibility = View.GONE
            binding.toolbar.title = "Manage Users"
        }

        // Force refresh data khi quay lại từ EditUserFragment
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBarAllUsers.visibility = View.VISIBLE
            delay(250) // Đợi animation màn hình hoàn tất
            viewModel.forceRefresh() // Luôn refresh để cập nhật dữ liệu mới nhất
        }
    }

    override fun cleanupViewReferences() {
        if (_binding != null) {
            binding.rcvAllUsers.adapter = null
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()
            _binding = null
        }
        super.cleanupViewReferences()
    }
}