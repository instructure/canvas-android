/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.pandautils.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class ViewPagerNonSwipeable : ViewPager {

    var canSwipe = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // Never allow swiping to switch between pages
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = if (canSwipe) super.onInterceptTouchEvent(event) else false

    // Never allow swiping to switch between pages
    override fun onTouchEvent(event: MotionEvent): Boolean = if (canSwipe) super.onTouchEvent(event) else false

    override fun canScrollHorizontally(direction: Int): Boolean = if (canSwipe) super.canScrollHorizontally(direction) else false
}
