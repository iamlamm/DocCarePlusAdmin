package  com.healthtech.doccareplusadmin.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.healthtech.doccareplusadmin.common.dialogs.CustomDialogFragment

// Extension Function cho Fragment
fun Fragment.showSuccessDialog(
    title: String,
    message: String,
    positiveText: String = "Đã hiểu",
    negativeText: String? = null,
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (!isAdded || requireActivity().isFinishing) return

    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.SUCCESS,
        cancelable = cancelable
    )

    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }

    dialog.show(childFragmentManager, "success_dialog")
}

fun Fragment.showErrorDialog(
    title: String = "Lỗi",
    message: String,
    positiveText: String = "Đã hiểu",
    negativeText: String? = null,
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (!isAdded || requireActivity().isFinishing) return

    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.ERROR,
        cancelable = cancelable
    )

    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }

    dialog.show(childFragmentManager, "error_dialog")
}

fun Fragment.showInfoDialog(
    title: String = "Thông báo",
    message: String,
    positiveText: String = "Đã hiểu",
    negativeText: String? = null,
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (!isAdded || requireActivity().isFinishing) return
    
    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.INFO,
        cancelable = cancelable
    )
    
    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }
    
    dialog.show(childFragmentManager, "info_dialog")
}

fun Fragment.showWarningDialog(
    title: String = "Cảnh báo",
    message: String,
    positiveText: String = "Đồng ý",
    negativeText: String = "Huỷ",
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (!isAdded || requireActivity().isFinishing) return
    
    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.WARNING,
        cancelable = cancelable
    )
    
    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }
    
    dialog.show(childFragmentManager, "warning_dialog")
}

// Thêm các extension functions tương tự cho InfoDialog và WarningDialog

// Extension Functions cho Activity
fun FragmentActivity.showSuccessDialog(
    title: String,
    message: String,
    positiveText: String = "Đã hiểu",
    negativeText: String? = null,
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (isFinishing || isDestroyed) return

    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.SUCCESS,
        cancelable = cancelable
    )

    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }

    dialog.show(supportFragmentManager, "success_dialog")
}

fun FragmentActivity.showErrorDialog(
    title: String = "Lỗi",
    message: String,
    positiveText: String = "Đã hiểu",
    negativeText: String? = null,
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (isFinishing || isDestroyed) return

    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.ERROR,
        cancelable = cancelable
    )

    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }

    dialog.show(supportFragmentManager, "error_dialog")
}

fun FragmentActivity.showInfoDialog(
    title: String = "Thông báo",
    message: String,
    positiveText: String = "Đã hiểu",
    negativeText: String? = null,
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (isFinishing || isDestroyed) return

    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.INFO,
        cancelable = cancelable
    )

    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }

    dialog.show(supportFragmentManager, "info_dialog")
}

fun FragmentActivity.showWarningDialog(
    title: String = "Cảnh báo",
    message: String,
    positiveText: String = "Đồng ý",
    negativeText: String = "Huỷ",
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    if (isFinishing || isDestroyed) return

    val dialog = CustomDialogFragment.newInstance(
        title = title,
        message = message,
        positiveText = positiveText,
        negativeText = negativeText,
        type = CustomDialogFragment.Type.WARNING,
        cancelable = cancelable
    )

    onPositive?.let { dialog.setPositiveButtonCallback(it) }
    onNegative?.let { dialog.setNegativeButtonCallback(it) }

    dialog.show(supportFragmentManager, "warning_dialog")
}