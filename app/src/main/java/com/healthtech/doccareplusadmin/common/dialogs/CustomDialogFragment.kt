package  com.healthtech.doccareplusadmin.common.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.DialogCustomBinding

class CustomDialogFragment : DialogFragment() {
    private var _binding: DialogCustomBinding? = null
    private val binding get() = _binding!!

    private var positiveButtonCallback: (() -> Unit)? = null
    private var negativeButtonCallback: (() -> Unit)? = null

    enum class Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_POSITIVE_TEXT = "positive_text"
        private const val ARG_NEGATIVE_TEXT = "negative_text"
        private const val ARG_TYPE = "type"
        private const val ARG_CANCELABLE = "cancelable"

        fun newInstance(
            title: String,
            message: String,
            positiveText: String = "Đã hiểu",
            negativeText: String? = null,
            type: Type = Type.INFO,
            cancelable: Boolean = true
        ): CustomDialogFragment {
            return CustomDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                    putString(ARG_POSITIVE_TEXT, positiveText)
                    negativeText?.let { putString(ARG_NEGATIVE_TEXT, it) }
                    putSerializable(ARG_TYPE, type)
                    putBoolean(ARG_CANCELABLE, cancelable)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCustomBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(arguments?.getBoolean(ARG_CANCELABLE) ?: true)

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialogContent()
        setupButtons()
    }

    private fun setupDialogContent() {
        arguments?.let { args ->
            // Thiết lập title và message
            binding.tvDialogTitle.text = args.getString(ARG_TITLE)
            binding.tvDialogMessage.text = args.getString(ARG_MESSAGE)

            // Thiết lập icon dựa vào type
            val type = args.getSerializable(ARG_TYPE) as Type
            val iconResId = when (type) {
                Type.SUCCESS -> R.drawable.ic_success
                Type.ERROR -> R.drawable.ic_error
                Type.WARNING -> R.drawable.ic_warning
                Type.INFO -> R.drawable.ic_info
            }

            binding.ivDialogIcon.setImageResource(iconResId)

            // Thiết lập màu icon
            val colorResId = when (type) {
                Type.SUCCESS -> R.color.success
                Type.ERROR -> R.color.error
                Type.WARNING -> R.color.warning
                Type.INFO -> R.color.primary
            }

            binding.ivDialogIcon.setColorFilter(
                ContextCompat.getColor(requireContext(), colorResId)
            )
        }
    }

    private fun setupButtons() {
        arguments?.let { args ->
            // Thiết lập nút positive
            binding.btnDialogPositive.text = args.getString(ARG_POSITIVE_TEXT)
            binding.btnDialogPositive.setOnClickListener {
                positiveButtonCallback?.invoke()
                dismiss()
            }

            // Thiết lập nút negative (nếu có)
            val negativeText = args.getString(ARG_NEGATIVE_TEXT)
            if (!negativeText.isNullOrEmpty()) {
                binding.btnDialogNegative.visibility = View.VISIBLE
                binding.btnDialogNegative.text = negativeText
                binding.btnDialogNegative.setOnClickListener {
                    negativeButtonCallback?.invoke()
                    dismiss()
                }
            } else {
                binding.btnDialogNegative.visibility = View.GONE
            }
        }
    }

    fun setPositiveButtonCallback(callback: () -> Unit) {
        positiveButtonCallback = callback
    }

    fun setNegativeButtonCallback(callback: () -> Unit) {
        negativeButtonCallback = callback
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}