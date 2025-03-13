package com.healthtech.doccareplusadmin.ui.category

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.common.base.BaseFragment
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.databinding.FragmentEditCategoryBinding
import com.healthtech.doccareplusadmin.domain.model.Category
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditCategoryFragment : BaseFragment() {
    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditCategoryViewModel by viewModels()
    private val args: EditCategoryFragmentArgs by navArgs()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setSelectedImage(uri)
                loadImageFromUri(uri)
            }
        }
    }

    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupImagePicker()
        setupSaveButton()
        observeViewModel()

        // Load category data if editing
        viewModel.loadCategory(args.categoryId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = if (args.categoryId == null) "Add Category" else "Edit Category"
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupImagePicker() {
        binding.cardCategoryImage.setOnClickListener {
            openImagePicker()
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            val description = binding.etCategoryDescription.text.toString().trim()
            val code = binding.etCategoryCode.text.toString().trim()
            viewModel.saveCategory(name, description, code)
        }
    }

    private fun observeViewModel() {
        // Observe category state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        populateUI(state.data)
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        SnackbarUtils.showErrorSnackbar(binding.root, state.message)
                    }
                    else -> {
                        // Do nothing for Idle state
                    }
                }
            }
        }

        // Observe upload progress
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadProgress.collectLatest { progress ->
                when {
                    // Chỉ hiển thị dialog khi đang tải (>0 và <100)
                    progress in 1..99 -> {
                        if (progressDialog == null) {
                            progressDialog = ProgressDialog(requireContext()).apply {
                                setMessage("Uploading category image (${progress}%)...")
                                setCancelable(false)
                                show()
                            }
                        } else {
                            progressDialog?.setMessage("Uploading category image (${progress}%)...")
                        }
                    }
                    // Khi progress = 100 (upload hoàn tất)
                    progress == 100 -> {
                        progressDialog?.dismiss()
                        progressDialog = null
                        
                        // Hiển thị thông báo thành công CHỈ khi có ảnh đã được chọn
                        if (viewModel.selectedImageUri.value != null) {
                            SnackbarUtils.showSuccessSnackbar(
                                binding.root,
                                "Image uploaded successfully"
                            )
                        }
                        
                        // Reset giá trị
                        viewModel.resetUploadProgress()
                        // Đặt selectedImageUri về null sau khi đã upload xong
                        viewModel.clearSelectedImage()
                    }
                    // Khi progress = 0 (không có upload hoặc đã reset)
                    progress == 0 -> {
                        progressDialog?.dismiss()
                        progressDialog = null
                    }
                }
            }
        }

        // Observe save state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSaveCategory.isEnabled = false
                        SnackbarUtils.showInfoSnackbar(binding.root, "Saving category...")
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveCategory.isEnabled = true
                        
                        val rootView = requireActivity().window.decorView.findViewById<View>(android.R.id.content)
                        
                        SnackbarUtils.showSuccessSnackbar(
                            rootView,
                            "Category saved successfully"
                        )
                        
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(1000)
                            findNavController().navigateUp()
                        }
                        
                        viewModel.resetSaveState()
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveCategory.isEnabled = true
                        
                        showErrorDialog(
                            title = "Error",
                            message = state.message,
                            positiveText = "OK"
                        )
                        
                        viewModel.resetSaveState()
                    }
                    else -> {
                        binding.btnSaveCategory.isEnabled = true
                    }
                }
            }
        }
    }

    private fun populateUI(category: Category) {
        binding.apply {
            etCategoryName.setText(category.name)
            etCategoryDescription.setText(category.description ?: "")
            etCategoryCode.setText(category.code ?: "")

            // Load image
            Glide.with(requireContext())
                .load(category.icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.cardiology)
                .into(ivCategoryImage)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun loadImageFromUri(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(R.drawable.cardiology)
            .into(binding.ivCategoryImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog?.dismiss()
        progressDialog = null
        viewModel.resetSaveState()
        viewModel.resetUploadProgress()
        _binding = null
    }

    override fun cleanupViewReferences() {
        _binding = null
        super.cleanupViewReferences()
    }
}