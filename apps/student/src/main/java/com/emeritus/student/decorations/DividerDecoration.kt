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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emeritus.student.R

class DividerDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val divider: Drawable = ContextCompat.getDrawable(context, R.drawable.line_divider)!!

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) < 1) return
        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.top = divider.intrinsicHeight
        } else {
            outRect.left = divider.intrinsicWidth
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        val size: Int

        val orientation = getOrientation(parent)
        val childCount = parent.childCount

        if (orientation == LinearLayoutManager.VERTICAL) {
            size = divider.intrinsicHeight
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
        } else { // horizontal
            size = divider.intrinsicWidth
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
        }

        for (i in 1 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.top - params.topMargin
                bottom = top + size
            } else { // horizontal
                left = child.left - params.leftMargin
                right = left + size
            }

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    private fun getOrientation(parent: RecyclerView): Int {
        return (parent.layoutManager as? LinearLayoutManager)?.orientation
            ?: throw IllegalStateException("DividerDecoration can only be used with a LinearLayoutManager.")
    }

}
