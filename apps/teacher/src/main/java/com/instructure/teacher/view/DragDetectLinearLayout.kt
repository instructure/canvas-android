/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 */    package com.instructure.teacher.view

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.LinearLayout

class DragDetectLinearLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val touchSlop by lazy { ViewConfiguration.get(context).scaledTouchSlop }

    private val touchOrigin = PointF()

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(ev.x - touchOrigin.x) >= touchSlop || Math.abs(ev.y - touchOrigin.y) >= touchSlop) {
                    return true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                touchOrigin.x = ev.x
                touchOrigin.y = ev.y
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}
