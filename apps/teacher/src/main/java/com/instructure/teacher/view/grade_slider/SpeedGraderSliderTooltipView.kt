/*
 * Copyright (C) 2020 - present  Instructure, Inc.
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
package com.instructure.teacher.view.grade_slider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import com.instructure.pandautils.utils.positionOnScreen
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.view.TooltipView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SpeedGraderSliderTooltipView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TooltipView(context, attrs, defStyleAttr) {

    /** Subscription to [ShowSliderGradeEvent] */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showTip(event: ShowSliderGradeEvent) {
        if (event.seekBar == null) {
            hideTip()
        } else if (event.assigneeId == assigneeId) {
            val thumbRect = event.seekBar.thumb.bounds
            val rect = Rect(thumbRect)
            //This is needed because of the weird thumb boundaries
            rect.left = thumbRect.left + (3 * thumbRect.width()) / 2
            showTip(event.description, rect)
        }
    }

    override fun shouldShowTutorial(): Boolean = false

    override fun drawTutorialView(canvas: Canvas) {
    }
}