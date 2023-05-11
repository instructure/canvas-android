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
package com.emeritus.student.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout

// Used in assignment_list_layout.xml
@Suppress("unused")
class PatchedScrollingViewBehavior : AppBarLayout.ScrollingViewBehavior {
    constructor() : super()
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        if (child.layoutParams.height != -1) return false

        val appBar = parent.getDependencies(child)
            .filterIsInstance<AppBarLayout>()
            .firstOrNull { it.isLaidOut }
            ?: return false

        if (ViewCompat.getFitsSystemWindows(appBar)) child.fitsSystemWindows = true
        val scrollRange = appBar.totalScrollRange
        val parentHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec)
        val height = parentHeight - appBar.measuredHeight + scrollRange
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
        parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed)
        return true
    }
}
