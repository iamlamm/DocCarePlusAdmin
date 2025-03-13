package  com.healthtech.doccareplusadmin.common.base

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.healthtech.doccareplusadmin.common.state.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * Khi chuyển từ HomeFragment sang Fragment khác:
 * 1. Framework calls onDestroyView()
 * 2. BaseFragment.onDestroyView() được gọi
 * 3. BaseFragment gọi HomeFragment.cleanupResources()
 * 4. HomeFragment cleanup tất cả resources
 * 5. BaseFragment gọi super.onDestroyView()
 */

/**
 * Base Fragment với các utility functions để quản lý lifecycle, ViewBinding và UI state.
 *
 * Lifecycle:
 * - onCreate: Khởi tạo các thành phần không liên quan đến View (adapters, ViewModel, etc.)
 * - onViewCreated: Setup UI và observe data
 * - onDestroyView: Cleanup view references để tránh memory leaks
 */

abstract class BaseFragment : Fragment() {
    /**
     * Xử lý các UI state một cách thống nhất
     */
    protected fun <T> handleUiState(
        state: UiState<T>,
        onIdle: () -> Unit = {},
        onLoading: () -> Unit = {},
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit
    ) {
        when (state) {
            is UiState.Idle -> onIdle()
            is UiState.Loading -> onLoading()
            is UiState.Success -> onSuccess(state.data)
            is UiState.Error -> onError(state.message)
        }
    }

    /**
     * Helper method để collect Flow với lifecycle awareness
     */
    protected fun <T> Flow<T>.collectWithLifecycle(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        collector: (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(lifecycleState) {
                collect { collector(it) }
            }
        }
    }

    /**
     * Helper method để collectLatest Flow với lifecycle awareness
     */
    protected fun <T> Flow<T>.collectLatestWithLifecycle(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        collector: (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(lifecycleState) {
                collectLatest { collector(it) }
            }
        }
    }

    /**
     * Clean up các view references để tránh memory leaks.
     * Lớp con không nên set adapter instances = null, chỉ nên clear references.
     */
    @CallSuper
    protected open fun cleanupViewReferences() {
        // Implement trong lớp con
    }

    @CallSuper
    override fun onDestroyView() {
        cleanupViewReferences()
        super.onDestroyView()
    }
}