/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.teacher.features.calendarevent

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.calendarevent.details.EventRouter

class TeacherEventRouter(private val activity: FragmentActivity) : EventRouter {

    override fun openLtiScreen(canvasContext: CanvasContext?, url: String) {
        TODO("Not yet implemented")
    }

    override fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?) {
        TODO("Not yet implemented")
    }

    override fun openEditEvent(scheduleItem: ScheduleItem) {
        TODO("Not yet implemented")
    }
}
