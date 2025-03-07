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
package com.instructure.parentapp.features.calendarevent

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.calendarevent.details.EventRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.parentapp.util.navigation.Navigation

class ParentEventRouter(private val activity: FragmentActivity, private val navigation: Navigation) : EventRouter {

    override fun openEditEvent(scheduleItem: ScheduleItem) {
        navigation.navigate(activity, navigation.updateEventRoute(scheduleItem))
    }

    override fun navigateToComposeMessageScreen(options: InboxComposeOptions) {
        val route = navigation.inboxComposeRoute(options)
        navigation.navigate(activity, route)
    }
}
