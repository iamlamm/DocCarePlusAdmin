package  com.healthtech.doccareplusadmin.ui.widgets.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.healthtech.doccareplusadmin.R

class CustomItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val spacing = context.resources.getDimensionPixelSize(R.dimen.notification_item_spacing)
    private val divider: Drawable = ContextCompat.getDrawable(context, R.drawable.custom_divider)!!

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position != RecyclerView.NO_POSITION) {
            outRect.bottom = spacing
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight

        // Không vẽ divider cho item cuối cùng
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + divider.intrinsicHeight

            divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            divider.draw(canvas)
        }
    }
}