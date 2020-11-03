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
 */
package com.instructure.teacher.view.edit_rubric

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.instructure.pandautils.utils.positionOnScreen
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.view.TooltipView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RubricTooltipView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TooltipView(context, attrs, defStyleAttr) {

    /** Subscription to [ShowRatingDescriptionEvent] */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showTip(event: ShowRatingDescriptionEvent) {
        if (!TeacherPrefs.hasViewedRubricTutorial && !event.isTutorialTip) {
            TeacherPrefs.hasViewedRubricTutorial = true
            hideTip()
        }
        if (event.anchor == null) {
            hideTip()
        } else if (event.assigneeId == assigneeId) {
            val (x, y) = event.anchor.positionOnScreen
            val (thisX, thisY) = positionOnScreen
            val anchorX = x - thisX + (event.anchor.width / 2)
            val anchorY = y - thisY + computeVerticalScrollOffset()
            val rect = Rect(anchorX, anchorY, anchorX + event.anchor.width, anchorY + event.anchor.height)
            showTip(event.description, rect)
        }
    }

}
