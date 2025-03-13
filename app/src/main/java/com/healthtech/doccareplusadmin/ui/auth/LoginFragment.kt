package  com.healthtech.doccareplusadmin.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.FragmentLoginBinding
import com.healthtech.doccareplusadmin.utils.ValidationUtils
import com.healthtech.doccareplusadmin.utils.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    private var hasEmailFocused = false
    private var hasPasswordFocused = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFocusListeners()
        setupTextWatchers()
        setupClickListeners()
        observeLoginState()
        observeRememberMeState()
    }

    private fun setupFocusListeners() {
        binding.apply {
            etLoginEmail.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) hasEmailFocused = true
            }
            etLoginPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) hasPasswordFocused = true
            }
        }
    }

    private fun createTextWatcher(validateFunction: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                validateFunction()
                updateButtonState()
            }

        }
    }

    private fun setupTextWatchers() {
        binding.apply {
            etLoginEmail.addTextChangedListener(createTextWatcher { validateEmail() })
            etLoginPassword.addTextChangedListener(createTextWatcher { validatePassword() })
        }
    }

    private fun setupClickListeners() {
        binding.btnLoginSubmit.setOnClickListener {
            hideKeyboard()
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()
            val rememberMe = binding.cbRememberMe.isChecked
            binding.btnLoginSubmit.isEnabled = false
            binding.progressBarLogin.visibility = View.VISIBLE
            viewModel.login(email, password, rememberMe)
        }


        binding.cbRememberMe.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateRememberMe(isChecked)
        }

    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> {
                        binding.progressBarLogin.setLoading(true)
                        binding.btnLoginSubmit.isEnabled = false
                    }

                    is LoginState.Success -> {
                        binding.progressBarLogin.setLoading(false)
                        binding.btnLoginSubmit.isEnabled = true
                        findNavController().navigate(R.id.action_login_to_dashboard)
                    }

                    is LoginState.Error, is LoginState.InvalidRole -> {
                        binding.progressBarLogin.setLoading(false)
                        binding.btnLoginSubmit.isEnabled = true
                        showErrorDialog(
                            title = "Thông báo",
                            message = when(state) {
                                is LoginState.Error -> state.message
                                is LoginState.InvalidRole -> state.message
                                else -> "Đã xảy ra lỗi"
                            },
                            positiveText = "Đã hiểu",
                            onPositive = {
                                viewModel.resetLoginState()
                            }
                        )
                    }

                    else -> {
                        binding.progressBarLogin.setLoading(false)
                        binding.btnLoginSubmit.isEnabled = true
                    }
                }
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etLoginEmail.text.toString()
        return when {
            !hasEmailFocused && email.isEmpty() -> {
                binding.tilLoginEmail.error = null
                false
            }

            hasEmailFocused && email.isEmpty() -> {
                binding.tilLoginEmail.error = getString(R.string.email_required)
                false
            }

            !ValidationUtils.isValidEmail(email) -> {
                binding.tilLoginEmail.error = getString(R.string.invalid_email)
                false
            }

            else -> {
                binding.tilLoginEmail.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding.etLoginPassword.text.toString()
        return when {
            !hasPasswordFocused && password.isEmpty() -> {
                binding.tilLoginPassword.error = null
                false
            }

            hasPasswordFocused && password.isEmpty() -> {
                binding.tilLoginPassword.error = getString(R.string.password_required)
                false
            }

            !ValidationUtils.isValidPassword(password) -> {
                binding.tilLoginPassword.error = getString(R.string.invalid_password)
                false
            }

            else -> {
                binding.tilLoginPassword.error = null
                true
            }
        }
    }

    private fun updateButtonState() {
        binding.btnLoginSubmit.isEnabled = validateEmail() && validatePassword()
    }

    private fun observeRememberMeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rememberMeState.collect { isChecked ->
                binding.cbRememberMe.isChecked = isChecked
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = requireActivity().currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}