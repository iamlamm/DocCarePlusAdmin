package com.healthtech.doccareplusadmin.ui.user

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
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.common.base.BaseFragment
import com.healthtech.doccareplusadmin.common.state.UiState
import com.healthtech.doccareplusadmin.databinding.FragmentEditUserBinding
import com.healthtech.doccareplusadmin.domain.model.Gender
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.domain.model.UserRole
import com.healthtech.doccareplusadmin.utils.SnackbarUtils
import com.healthtech.doccareplusadmin.utils.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class EditUserFragment : BaseFragment() {
    private var _binding: FragmentEditUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserViewModel by viewModels()
    private val args: EditUserFragmentArgs by navArgs()

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
    private var selectedGender: Gender? = null
    private var selectedRole: UserRole = UserRole.PATIENT

    // Arrays for dropdown menus
    private val bloodTypes = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    private val genders = Gender.values().map { it.name }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDropdowns()
        setupImagePicker()
        setupSaveButton()
        observeViewModel()

        // Load user data if editing
        viewModel.loadUser(args.userId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = if (args.userId == null) "Add User" else "Edit User"
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupDropdowns() {
        // Set up dropdown adapters
        val bloodTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            bloodTypes
        )
        binding.actUserBloodType.setAdapter(bloodTypeAdapter)

        val genderAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            genders
        )
        binding.actUserGender.setAdapter(genderAdapter)
        binding.actUserGender.setOnItemClickListener { _, _, position, _ ->
            selectedGender = Gender.values()[position]
        }
    }

    private fun setupImagePicker() {
        binding.cardUserImage.setOnClickListener {
            openImagePicker()
        }
        binding.btnChangeImage.setOnClickListener {
            openImagePicker()
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveUser.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            val email = binding.etUserEmail.text.toString().trim()
            val phone = binding.etUserPhone.text.toString().trim()
            val age = binding.etUserAge.text.toString().trim()
            val bloodType = binding.actUserBloodType.text.toString().trim()
            val height = binding.etUserHeight.text.toString().trim().toIntOrNull()
            val weight = binding.etUserWeight.text.toString().trim().toIntOrNull()
            val about = binding.etUserAbout.text.toString().trim()

            viewModel.saveUser(
                name = name,
                email = email,
                phoneNumber = phone,
                role = selectedRole,
                age = age,
                gender = selectedGender,
                bloodType = if (bloodType.isBlank()) null else bloodType,
                height = height,
                weight = weight,
                about = if (about.isBlank()) null else about
            )
        }
    }

    private fun observeViewModel() {
        // Observe user state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userState.collectLatest { state ->
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
                        showErrorDialog(
                            title = "Error",
                            message = state.message,
                            positiveText = "OK"
                        )
                    }
                    else -> {}
                }
            }
        }

        // Observe save state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSaveUser.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveUser.isEnabled = true
                        
                        val rootView = requireActivity().window.decorView.findViewById<View>(android.R.id.content)
                        
                        SnackbarUtils.showSuccessSnackbar(
                            rootView,
                            "User saved successfully"
                        )
                        
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(1000)
                            findNavController().navigateUp()
                        }
                        
                        viewModel.resetSaveState()
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveUser.isEnabled = true
                        
                        showErrorDialog(
                            title = "Error",
                            message = state.message,
                            positiveText = "OK"
                        )
                        
                        viewModel.resetSaveState()
                    }
                    else -> {
                        binding.btnSaveUser.isEnabled = true
                    }
                }
            }
        }

        // Observe upload progress
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadProgress.collect { progress ->
                Timber.d("Upload progress: $progress")
                when {
                    progress in 1..99 -> {
                        if (progressDialog == null) {
                            progressDialog = ProgressDialog(requireContext()).apply {
                                setMessage("Uploading user image (${progress}%)...")
                                setCancelable(false)
                                show()
                            }
                        } else {
                            progressDialog?.setMessage("Uploading user image (${progress}%)...")
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

    private fun populateUI(user: User) {
        binding.apply {
            etUserName.setText(user.name)
            etUserEmail.setText(user.email)
            etUserPhone.setText(user.phoneNumber)
            etUserAge.setText(user.age?.toString() ?: "")
            
            // For dropdown fields, set the text directly
            user.bloodType?.let { actUserBloodType.setText(it, false) }
            user.gender?.let { actUserGender.setText(it.name, false) }
            
            // Additional fields if available in your User model
            user.height?.let { etUserHeight.setText(it.toString()) }
            user.weight?.let { etUserWeight.setText(it.toString()) }
            user.about?.let { etUserAbout.setText(it) }

            // Set gender dropdown
            user.gender?.let { gender ->
                selectedGender = gender
            }

            // Role is already set as default to PATIENT if not specified

            // Log để debug
            Timber.d("Loading avatar URL: ${user.avatar}")
            
            // Cải thiện cách load avatar
            Glide.with(requireContext())
                .load(user.avatar?.takeIf { it.isNotBlank() } ?: R.mipmap.avatar_male_default)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.avatar_male_default)
                .error(R.mipmap.avatar_male_default)
                .into(ivUserImage)
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
            .error(R.mipmap.avatar_male_default)
            .into(binding.ivUserImage)
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