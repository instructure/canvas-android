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

package com.instructure.student.features.calendarevent.details

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment
import com.instructure.pandautils.features.calendarevent.details.EventRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.student.router.RouteMatcher

class StudentEventRouter(private val activity: FragmentActivity) : EventRouter {

    override fun openEditEvent(scheduleItem: ScheduleItem) {
        val route = CreateUpdateEventFragment.makeRoute(scheduleItem)
        RouteMatcher.route(activity, route)
    }

    override fun navigateToComposeMessageScreen(options: InboxComposeOptions) = Unit
}
