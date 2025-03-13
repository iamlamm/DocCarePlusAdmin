package  com.healthtech.doccareplusadmin.ui.doctor

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.common.base.BaseFragment
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.databinding.FragmentAllDoctorsBinding
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.ui.doctor.adapter.AllDoctorsAdapter
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showWarningDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AllDoctorsFragment : BaseFragment() {
    private var _binding: FragmentAllDoctorsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllDoctorsViewModel by viewModels()
    private lateinit var allDoctorsAdapter: AllDoctorsAdapter

    // Để theo dõi xem đã setup observers chưa để tránh setup lại
    private var hasSetupObservers = false

    // Theo dõi lần load đầu tiên để tối ưu hiệu suất
    private var isFirstLoad = true

    // Cập nhật onResume để refresh mạnh hơn
    private var onResumeInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllDoctorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hiển thị loading indicator ngay lập tức
        binding.progressBarAllDoctors.visibility = View.VISIBLE

        // Initialization in order of priority
        setupToolbar()
        setupAdapter()
        setupRecyclerView()
        setupSearchView()
        setupFab()
        setupBackPressHandling()
        
        // Đặt onResume để force refresh
        onResumeInitialized = false

        // Setup observers
        observeDoctors()
        observeSearchResults()
        observeDeleteState()
        observeCategoryInfo()
        observeSearchState()
    }

    private fun setupBackPressHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.searchView.visibility == View.VISIBLE) {
                        toggleSearchView()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun setupFab() {
        binding.fabAddDoctor.setOnClickListener {
            findNavController().navigate(
                AllDoctorsFragmentDirections.actionAllDoctorsToEditDoctor()
            )
        }
    }

    private fun toggleSearchView() {
        binding.searchView.apply {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
                viewModel.setSearchActive(false)
                binding.toolbar.title = viewModel.categoryName.value ?: "Manage Doctors"
            } else {
                visibility = View.VISIBLE
                viewModel.setSearchActive(true)
                binding.toolbar.title = ""
                requestFocus()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            queryHint = "Search doctors..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.setSearchQuery(newText ?: "")
                    return true
                }
            })
            setOnCloseListener {
                toggleSearchView()
                true
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                closeSearchAndNavigateBack()
            }
            title = "Manage Doctors"
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
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
        allDoctorsAdapter = AllDoctorsAdapter()
        
        // Click vào card bác sĩ để edit
        allDoctorsAdapter.setOnDoctorClickListener { doctor ->
            Timber.d("Doctor clicked: ${doctor.id} - ${doctor.name}")
            navigateToEditDoctor(doctor.id)
        }
        
        // Long click để hiển thị dialog xóa
        allDoctorsAdapter.setOnDoctorLongClickListener { doctor ->
            Timber.d("Doctor long clicked: ${doctor.id} - ${doctor.name}")
            showDeleteConfirmationDialog(doctor)
        }
        
        // Click vào nút edit (nếu có trong layout)
        allDoctorsAdapter.setOnEditClickListener { doctor ->
            Timber.d("Edit button clicked: ${doctor.id} - ${doctor.name}")
            navigateToEditDoctor(doctor.id)
        }
        
        // Click vào nút delete (nếu có trong layout)
        allDoctorsAdapter.setOnDeleteClickListener { doctor ->
            Timber.d("Delete button clicked: ${doctor.id} - ${doctor.name}")
            showDeleteConfirmationDialog(doctor)
        }
    }

    private fun navigateToEditDoctor(doctorId: Int) {
        val action = AllDoctorsFragmentDirections
            .actionAllDoctorsToEditDoctor(doctorId)
        findNavController().navigate(action)
    }
    
    private fun showDeleteConfirmationDialog(doctor: Doctor) {
        showWarningDialog(
            title = "Delete Doctor",
            message = "Are you sure you want to delete Dr. ${doctor.name}?",
            positiveText = "Delete",
            negativeText = "Cancel",
            onPositive = {
                viewModel.deleteDoctor(doctor.id)
            }
        )
    }

    private fun setupRecyclerView() {
        binding.rcvAllDoctors.apply {
            adapter = allDoctorsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun observeDoctors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.doctors.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBarAllDoctors.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                        // Không ẩn RecyclerView ngay để tránh màn hình trắng
                    }
                    is UiState.Success -> {
                        binding.progressBarAllDoctors.visibility = View.GONE
                        
                        // Log kết quả để debug
                        Timber.d("Loaded ${state.data.size} doctors")
                        
                        if (state.data.isEmpty()) {
                            binding.tvEmptyState.visibility = View.VISIBLE
                            binding.rcvAllDoctors.visibility = View.GONE
                        } else {
                            binding.tvEmptyState.visibility = View.GONE
                            binding.rcvAllDoctors.visibility = View.VISIBLE
                            // Chỉ cập nhật adapter nếu đang ở chế độ không tìm kiếm
                            if (!viewModel.isSearchActive.value) {
                                allDoctorsAdapter.setDoctors(state.data)
                            }
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBarAllDoctors.visibility = View.GONE
                        Timber.e("Error loading doctors: ${state.message}")
                        
                        // Hiển thị thông báo lỗi
                        SnackbarUtils.showErrorSnackbar(
                            binding.root,
                            state.message
                        )
                        
                        // Hiện thông báo không có doctor nếu RecyclerView trống
                        if (allDoctorsAdapter.itemCount == 0) {
                            binding.tvEmptyState.visibility = View.VISIBLE
                            binding.rcvAllDoctors.visibility = View.GONE
                        }
                    }
                    else -> {
                        // Không làm gì cho trạng thái khác
                    }
                }
            }
        }
    }

    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { doctors ->
                allDoctorsAdapter.setDoctors(doctors)
                
                // Hiển thị trạng thái trống nếu cần
                if (doctors.isEmpty() && viewModel.isSearchActive.value) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rcvAllDoctors.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rcvAllDoctors.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeDeleteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Hiển thị loading nếu cần
                        binding.progressBarAllDoctors.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBarAllDoctors.visibility = View.GONE
                        
                        // Hiển thị thông báo xóa thành công
                        SnackbarUtils.showSuccessSnackbar(
                            binding.root,
                            "Doctor deleted successfully"
                        )
                        
                        // Quan trọng: Reset trạng thái delete để tránh hiển thị lại
                        viewModel.resetDeleteState()
                        
                        // Force refresh để cập nhật danh sách
                        viewModel.forceRefresh()
                    }
                    is UiState.Error -> {
                        binding.progressBarAllDoctors.visibility = View.GONE
                        
                        // Hiển thị thông báo lỗi
                        SnackbarUtils.showErrorSnackbar(
                            binding.root,
                            state.message ?: "Failed to delete doctor"
                        )
                        
                        // Quan trọng: Reset trạng thái delete
                        viewModel.resetDeleteState()
                    }
                    else -> {
                        // Không làm gì với Idle state
                    }
                }
            }
        }
    }

    // Thêm hàm để observe thông tin category
    private fun observeCategoryInfo() {
        viewModel.categoryName.collectWithLifecycle { categoryName ->
            if (!categoryName.isNullOrEmpty()) {
                if (binding.searchView.visibility != View.VISIBLE) {
                    binding.toolbar.title = categoryName
                }
            }
        }
    }

    // Thêm hàm để observe trạng thái tìm kiếm
    private fun observeSearchState() {
        viewModel.isSearchActive.collectWithLifecycle { isActive ->
            if (isActive) {
                binding.searchView.visibility = View.VISIBLE
                binding.toolbar.title = ""
            } else {
                binding.searchView.visibility = View.GONE
                binding.toolbar.title = viewModel.categoryName.value ?: "Manage Doctors"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Reset deleteState khi quay lại fragment để tránh hiển thị lại thông báo cũ
        viewModel.resetDeleteState()
        
        // Khôi phục trạng thái SearchView
        viewModel.isSearchActive.value.let { isActive ->
            if (isActive) {
                if (binding.searchView.visibility != View.VISIBLE) {
                    binding.searchView.visibility = View.VISIBLE
                    binding.toolbar.title = ""
                }
            } else {
                binding.searchView.visibility = View.GONE
                binding.toolbar.title = viewModel.categoryName.value ?: "Manage Doctors"
            }
        }
        
        // Force refresh data khi quay lại từ EditDoctorFragment
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBarAllDoctors.visibility = View.VISIBLE
            delay(250) // Đợi animation màn hình hoàn tất
            viewModel.forceRefresh() // Luôn refresh để cập nhật dữ liệu mới nhất
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun closeSearchAndNavigateBack() {
        if (binding.searchView.visibility == View.VISIBLE) {
            binding.searchView.visibility = View.GONE
            viewModel.setSearchActive(false)
        }
        findNavController().navigateUp()
    }

    override fun cleanupViewReferences() {
        if (_binding != null) {
            binding.rcvAllDoctors.adapter = null
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()
            viewModel.setSearchActive(false)
            _binding = null
        }
        super.cleanupViewReferences()
    }
}