package com.healthtech.doccareplusadmin.ui.doctor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.common.base.BaseFragment
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.databinding.FragmentEditDoctorBinding
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class EditDoctorFragment : BaseFragment() {
    private var _binding: FragmentEditDoctorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditDoctorViewModel by viewModels()
    private val args: EditDoctorFragmentArgs by navArgs()

    private var progressDialog: ProgressDialog? = null
    private var selectedCategoryId: Int = -1

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupImagePicker()
        setupSaveButton()
        setupCategoryDropdown()
        observeViewModel()

        // Load doctor data if editing
        viewModel.loadDoctor(args.doctorId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = if (args.doctorId == -1) "Add Doctor" else "Edit Doctor"
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupImagePicker() {
        binding.ivDoctorImage.setOnClickListener {
            openImagePicker()
        }
        
        binding.btnChangeImage.setOnClickListener {
            openImagePicker()
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveDoctor.setOnClickListener {
            val name = binding.etDoctorName.text.toString().trim()
            val specialty = binding.etDoctorSpecialty.text.toString().trim()
            val fee = binding.etDoctorFee.text.toString().trim()
            val bio = binding.etDoctorBiography.text.toString().trim()
            val code = binding.etDoctorCode.text.toString().trim()
            val email = binding.etDoctorEmail.text.toString().trim()
            val phone = binding.etDoctorPhone.text.toString().trim()
            val emergency = binding.etDoctorEmergency.text.toString().trim()
            val address = binding.etDoctorAddress.text.toString().trim()
            val available = binding.switchDoctorAvailable.isChecked
            
            // Nếu category chưa được chọn, sử dụng categoryId từ doctor hiện tại
            val categoryId = if (selectedCategoryId != -1) selectedCategoryId else {
                (viewModel.doctorState.value as? UiState.Success)?.data?.categoryId ?: -1
            }
            
            if (categoryId == -1) {
                SnackbarUtils.showErrorSnackbar(binding.root, "Please select a category")
                return@setOnClickListener
            }
            
            viewModel.saveDoctor(
                name, specialty, fee, categoryId, bio, code, 
                email, phone, emergency, address, available
            )
        }
    }

    private fun setupCategoryDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest { categories ->
                if (categories.isNotEmpty()) {
                    Timber.d("Categories loaded: ${categories.size}")
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        categories.map { it.name }
                    )
                    binding.actDoctorCategory.setAdapter(adapter)
                    
                    viewModel.currentDoctor.value?.let { doctor ->
                        val categoryIndex = categories.indexOfFirst { it.id == doctor.categoryId }
                        if (categoryIndex >= 0) {
                            binding.actDoctorCategory.setText(categories[categoryIndex].name, false)
                            selectedCategoryId = categories[categoryIndex].id
                        }
                    }
                    
                    binding.actDoctorCategory.setOnItemClickListener { _, _, position, _ ->
                        selectedCategoryId = categories[position].id
                        Timber.d("Selected category: ${categories[position].name} with ID: $selectedCategoryId")
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.doctorState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSaveDoctor.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveDoctor.isEnabled = true
                        populateUI(state.data)
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveDoctor.isEnabled = true
                        SnackbarUtils.showErrorSnackbar(binding.root, state.message)
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveDoctor.isEnabled = true
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSaveDoctor.isEnabled = false
                        Timber.d("Saving doctor...")
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        
                        // Hiển thị thông báo thành công
                        Timber.d("Doctor saved successfully")
                        SnackbarUtils.showSuccessSnackbar(
                            binding.root, 
                            "Doctor saved successfully",
                        )
                        
                        // Delay trước khi chuyển hướng để người dùng thấy thông báo
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(1000) // Đợi 1 giây
                            findNavController().navigateUp()
                        }
                        
                        viewModel.resetSaveState()
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveDoctor.isEnabled = true
                        
                        // Show error dialog
                        Timber.e("Error saving doctor: ${state.message}")
                        showErrorDialog(
                            title = "Error",
                            message = state.message,
                            positiveText = "OK"
                        )
                        
                        viewModel.resetSaveState()
                    }
                    else -> {
                        binding.btnSaveDoctor.isEnabled = true
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadProgress.collectLatest { progress ->
                Timber.d("Upload progress: $progress")
                when {
                    progress in 1..99 -> {
                        if (progressDialog == null) {
                            progressDialog = ProgressDialog(requireContext()).apply {
                                setMessage("Uploading doctor image (${progress}%)...")
                                setCancelable(false)
                                show()
                            }
                        } else {
                            progressDialog?.setMessage("Uploading doctor image (${progress}%)...")
                        }
                    }
                    progress == 100 -> {
                        progressDialog?.dismiss()
                        progressDialog = null
                        
                        // Hiển thị thông báo upload thành công
                        if (viewModel.selectedImageUri.value != null) {
                            SnackbarUtils.showSuccessSnackbar(
                                binding.root,
                                "Image uploaded successfully"
                            )
                            viewModel.clearSelectedImage()
                        }
                        
                        viewModel.resetUploadProgress()
                    }
                    progress == 0 -> {
                        progressDialog?.dismiss()
                        progressDialog = null
                    }
                }
            }
        }
    }

    private fun populateUI(doctor: Doctor) {
        binding.apply {
            etDoctorName.setText(doctor.name)
            etDoctorSpecialty.setText(doctor.specialty)
            etDoctorFee.setText(doctor.fee.toString())
            etDoctorBiography.setText(doctor.biography)
            etDoctorCode.setText(doctor.code)
            etDoctorEmail.setText(doctor.email)
            etDoctorPhone.setText(doctor.phoneNumber)
            etDoctorEmergency.setText(doctor.emergencyContact)
            etDoctorAddress.setText(doctor.address)
            switchDoctorAvailable.isChecked = doctor.available

            Glide.with(requireContext())
                .load(doctor.image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.doctor_avatar_1)
                .into(ivDoctorImage)
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
            .error(R.drawable.doctor_avatar_1)
            .into(binding.ivDoctorImage)
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