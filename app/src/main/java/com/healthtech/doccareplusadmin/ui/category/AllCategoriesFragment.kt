package  com.healthtech.doccareplusadmin.ui.category

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
import androidx.recyclerview.widget.GridLayoutManager
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.common.base.BaseFragment
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.databinding.FragmentAllCategoriesBinding
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.ui.category.adapter.AllCategoriesAdapter
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showWarningDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllCategoriesFragment : BaseFragment() {
    private var _binding: FragmentAllCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllCategoriesViewModel by viewModels()
    private lateinit var allCategoriesAdapter: AllCategoriesAdapter

    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupAdapter()
        setupRecyclerView()
        setupSearchView()
        setupFab()
        setupBackPressHandling()
        observeCategories()
        observeSearchResults()
        observeDeleteState()
    }

    private fun setupFab() {
        binding.fabAddCategory.setOnClickListener {
            findNavController().navigate(
                AllCategoriesFragmentDirections.actionAllCategoriesToEditCategory()
            )
        }
    }

    private fun setupBackPressHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.searchView.visibility == View.VISIBLE) {
                        binding.searchView.setQuery("", false)
                        hideKeyboard()
                        binding.searchView.clearFocus()
                        binding.searchView.visibility = View.GONE
                        viewModel.setSearchActive(false)
                        binding.toolbar.title = "Manage Categories"
                    } else {
                        // Cho phép hành vi back mặc định
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            })
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                closeSearchAndNavigateBack()
            }
            title = "Manage Categories"

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
        allCategoriesAdapter = AllCategoriesAdapter().apply {
            setOnCategoryClickListener { category ->
                navigateToEditCategory(category)
            }
            setOnCategoryLongClickListener { category ->
                showDeleteConfirmationDialog(category)
            }
        }
    }

    private fun navigateToEditCategory(category: Category) {
        category.id?.let { id ->
            findNavController().navigate(
                AllCategoriesFragmentDirections.actionAllCategoriesToEditCategory(id.toString())
            )
        }
    }

    private fun showDeleteConfirmationDialog(category: Category) {
        showWarningDialog(
            title = "Delete Category",
            message = "Are you sure you want to delete ${category.name}?",
            positiveText = "Delete", 
            negativeText = "Cancel",
            onPositive = {
                category.id?.let { id ->
                    viewModel.deleteCategory(id.toString())
                }
            }
        )
    }

    private fun setupRecyclerView() {
        binding.rcvAllCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = allCategoriesAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.setSearchQuery(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { viewModel.setSearchQuery(it) }
                    return true
                }
            })

            setOnCloseListener {
                viewModel.clearSearch()
                false
            }
        }
    }

    private fun observeCategories() {
        viewModel.categories.collectWithLifecycle { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarAllCategories.visibility = View.VISIBLE
                    binding.rcvAllCategories.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBarAllCategories.visibility = View.GONE
                    
                    val categories = state.data
                    if (categories.isEmpty()) {
                        binding.rcvAllCategories.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.tvEmptyState.text = "No categories found"
                    } else {
                        binding.rcvAllCategories.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                        
                        // Animate only on first load
                        if (isFirstLoad) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                delay(150)
                                allCategoriesAdapter.setCategories(categories)
                                isFirstLoad = false
                            }
                        } else {
                            allCategoriesAdapter.setCategories(categories)
                            if (isFirstLoad) isFirstLoad = false
                        }
                    }
                }

                is UiState.Error -> {
                    binding.progressBarAllCategories.visibility = View.GONE
                    binding.rcvAllCategories.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = state.message
                    
                    // Hiển thị thông báo lỗi
                    SnackbarUtils.showErrorSnackbar(binding.root, state.message)
                }

                else -> {
                    // Xử lý các trạng thái khác
                }
            }
        }
    }

    private fun observeSearchResults() {
        viewModel.searchResults.collectWithLifecycle { results ->
            allCategoriesAdapter.setCategories(results)
            
            // Hiển thị empty state nếu không có kết quả
            if (results.isEmpty() && viewModel.isSearchActive.value) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.tvEmptyState.text = "No matching categories found"
                binding.rcvAllCategories.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rcvAllCategories.visibility = View.VISIBLE
            }
        }
    }

    private fun observeDeleteState() {
        viewModel.deleteState.collectWithLifecycle { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarAllCategories.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.progressBarAllCategories.visibility = View.GONE
                    SnackbarUtils.showSuccessSnackbar(binding.root, "Category deleted successfully")
                }
                is UiState.Error -> {
                    binding.progressBarAllCategories.visibility = View.GONE
                    SnackbarUtils.showErrorSnackbar(binding.root, state.message)
                }
                else -> {
                    // Do nothing for Idle state
                }
            }
        }
    }

    // Toggle hiển thị SearchView
    private fun toggleSearchView() {
        binding.searchView.apply {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
                viewModel.setSearchActive(false)
                binding.toolbar.title = "Manage Categories"
            } else {
                visibility = View.VISIBLE
                viewModel.setSearchActive(true)
                binding.toolbar.title = ""
                requestFocus()
            }
        }
    }

    // Helper method để ẩn bàn phím
    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun closeSearchAndNavigateBack() {
        if (binding.searchView.visibility == View.VISIBLE) {
            binding.searchView.visibility = View.GONE
        }
        findNavController().navigateUp()
    }

    // Thêm vào onResume để đảm bảo dữ liệu được tải khi quay lại fragment
    override fun onResume() {
        super.onResume()

        // Khôi phục trạng thái SearchView dựa trên isSearchActive từ ViewModel
        viewModel.isSearchActive.value.let { isActive ->
            if (isActive) {
                if (binding.searchView.visibility != View.VISIBLE) {
                    binding.searchView.visibility = View.VISIBLE
                    binding.toolbar.title = ""
                }
            } else {
                binding.searchView.visibility = View.GONE
                binding.toolbar.title = "Manage Categories"
            }
        }

        // Kiểm tra và làm mới dữ liệu nếu cần
        viewModel.checkAndRefreshIfNeeded()
    }

    override fun cleanupViewReferences() {
        if (_binding != null) {
            binding.rcvAllCategories.adapter = null
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()
            _binding = null
        }
        super.cleanupViewReferences()
    }
}