/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.decorations

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.teacher.R

class VerticalGridSpacingDecoration(
    context: Context,
    val layoutManager: GridLayoutManager,
    @DimenRes horizontalSpacingResId: Int = R.dimen.default_grid_spacing,
    @DimenRes verticalSpacingResId: Int = R.dimen.default_grid_spacing
) : RecyclerView.ItemDecoration() {

    init {
        if (layoutManager.orientation != GridLayoutManager.VERTICAL) throw IllegalArgumentException("Provided GridLayoutManager must use a vertical orientation")
    }

    private val horizontalSpacing by lazy { context.resources.getDimensionPixelOffset(horizontalSpacingResId) }
    private val verticalSpacing by lazy { context.resources.getDimensionPixelOffset(verticalSpacingResId) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            left = horizontalSpacing
            right = horizontalSpacing
            top = verticalSpacing
            bottom = verticalSpacing
        }
    }
}
