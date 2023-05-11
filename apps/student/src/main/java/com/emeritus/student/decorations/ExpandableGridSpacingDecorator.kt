/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.emeritus.student.decorations

import android.graphics.Rect
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandarecycler.BaseExpandableRecyclerAdapter

class ExpandableGridSpacingDecorator(private val spacing: Int) : RecyclerView.ItemDecoration() {
    private val halfSpacing = spacing / 2

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val adapter = parent.adapter as BaseExpandableRecyclerAdapter<*, *, *>
        val position = parent.getChildAdapterPosition(view)
        val isHeader = adapter.isPositionGroupHeader(position)
        val viewHolder = parent.getChildViewHolder(view)
        if (position == RecyclerView.NO_POSITION) {
            // If this ItemDecoration does not affect the positioning of item views,
            val anim = AlphaAnimation(1.0f, 0.0f)
            anim.duration = 175
            viewHolder.itemView.startAnimation(anim)
            viewHolder.itemView.visibility = View.GONE
            return
        } else {
            viewHolder.itemView.visibility = View.VISIBLE
        }
        if (isHeader) return
        val spanCount = getTotalSpan(parent)
        val pseudoGroupPosition = adapter.getGroupVisualPosition(position) // the visual position
        val rows = (adapter.getGroupItemCount(pseudoGroupPosition) - 1) / spanCount + 1
        val leftRightEdge = getIsEdgeType(spanCount, position, pseudoGroupPosition)
        val topBottomEdge = getIsTopBottomEdgeType(spanCount, rows, position, pseudoGroupPosition)
        if (spanCount == 1) {
            outRect.left = spacing
            outRect.right = spacing
        } else {
            when (leftRightEdge) {
                LEFT -> {
                    outRect.left = spacing
                    outRect.right = halfSpacing
                }
                RIGHT -> {
                    outRect.left = halfSpacing
                    outRect.right = spacing
                }
                else -> {
                    outRect.left = halfSpacing
                    outRect.right = halfSpacing
                }
            }
        }
        if (rows == 1) {
            outRect.top = spacing
            outRect.bottom = spacing
        } else {
            when (topBottomEdge) {
                TOP -> {
                    outRect.top = spacing
                    outRect.bottom = halfSpacing
                }
                BOTTOM -> {
                    outRect.top = halfSpacing
                    outRect.bottom = spacing
                }
                else -> {
                    outRect.top = halfSpacing
                    outRect.bottom = halfSpacing
                }
            }
        }
    }

    private fun getTotalSpan(parent: RecyclerView): Int {
        return (parent.layoutManager as? GridLayoutManager)?.spanCount ?: -1
    }

    private fun getIsEdgeType(spanCount: Int, position: Int, pseudoGroupPosition: Int): Int {
        return when ((position - (pseudoGroupPosition + 1)) % spanCount) {
            0 -> LEFT
            spanCount - 1 -> RIGHT
            else -> NONE
        }
    }

    private fun getIsTopBottomEdgeType(spanCount: Int, rows: Int, position: Int, pseudoGroupPosition: Int): Int {
        return when ((position - (pseudoGroupPosition + 1)) / spanCount) {
            0 -> TOP
            rows - 1 -> BOTTOM
            else -> NONE
        }
    }

    companion object {
        private const val NONE = 0
        private const val LEFT = 1
        private const val RIGHT = 2
        private const val TOP = 3
        private const val BOTTOM = 4
    }
}
