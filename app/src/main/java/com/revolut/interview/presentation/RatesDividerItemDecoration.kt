package com.revolut.interview.presentation

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Task 4 divider implementation.
 *
 * Draws row separators at the RecyclerView level instead of adding a separator View
 * to every item layout. This keeps item layouts simpler and avoids drawing a divider
 * after the final row.
 */
class RatesDividerItemDecoration(
    dividerColor: Int,
    private val dividerHeight: Int
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        color = dividerColor
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount

        for (index in 0 until childCount) {
            val child = parent.getChildAt(index)
            val position = parent.getChildAdapterPosition(child)

            if (position == RecyclerView.NO_POSITION || position == state.itemCount - 1) {
                continue
            }

            val top = child.bottom.toFloat()
            val bottom = top + dividerHeight
            canvas.drawRect(
                parent.paddingLeft.toFloat(),
                top,
                (parent.width - parent.paddingRight).toFloat(),
                bottom,
                paint
            )
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        if (position != RecyclerView.NO_POSITION && position != state.itemCount - 1) {
            outRect.bottom = dividerHeight
        }
    }
}
