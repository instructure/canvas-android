/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.views

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

