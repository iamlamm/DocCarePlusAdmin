package  com.healthtech.doccareplusadmin.ui.widgets.behavior

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BottomAppBarFABBehavior(context: Context, attrs: AttributeSet?) :
    FloatingActionButton.Behavior(context, attrs) {

    private var fabHidden = false
    private var isAnimating = false

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout, child, target,
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            type, consumed
        )

        if (isAnimating) return

        // Enhanced animations
        if (dyConsumed > 10 && !fabHidden) {
            // Hide animation with subtle scale and translation
            hideFabWithAnimation(child)
        } else if (dyConsumed < -10 && fabHidden) {
            // Show animation with subtle scale and translation
            showFabWithAnimation(child)
        }
    }

    private fun hideFabWithAnimation(fab: FloatingActionButton) {
        isAnimating = true
        fabHidden = true

        // Create scale and translation animations
        val scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0.8f)
        val scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0.8f)
        val translationY = ObjectAnimator.ofFloat(fab, "translationY", 0f, fab.height * 1.5f)

        // Combine animations
        val animSet = AnimatorSet()
        animSet.playTogether(scaleX, scaleY, translationY)
        animSet.duration = 250
        animSet.interpolator = DecelerateInterpolator()
        animSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                isAnimating = false
            }
        })

        animSet.start()
    }

    private fun showFabWithAnimation(fab: FloatingActionButton) {
        isAnimating = true
        fabHidden = false

        // Create scale and translation animations
        val scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 0.8f, 1f)
        val translationY = ObjectAnimator.ofFloat(fab, "translationY", fab.height * 1.5f, 0f)

        // Combine animations
        val animSet = AnimatorSet()
        animSet.playTogether(scaleX, scaleY, translationY)
        animSet.duration = 250
        animSet.interpolator = DecelerateInterpolator()
        animSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                isAnimating = false
            }
        })

        animSet.start()
    }
}