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
package com.instructure.teacher.features.calendar

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.features.calendar.CalendarRouter

class TeacherCalendarRouter(val activity: FragmentActivity) : CalendarRouter {
    override fun openNavigationDrawer() {

    }

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        TODO("Not yet implemented")
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long) {
        TODO("Not yet implemented")
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        TODO("Not yet implemented")
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long) {
        TODO("Not yet implemented")
    }

    override fun openToDo(plannerItem: PlannerItem) {
        TODO("Not yet implemented")
    }

    override fun openCreateToDo(initialDateString: String?) {
        TODO("Not yet implemented")
    }
}