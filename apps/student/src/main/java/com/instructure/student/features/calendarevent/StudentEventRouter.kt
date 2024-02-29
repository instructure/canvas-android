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

package com.instructure.student.features.calendarevent

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.calendarevent.details.EventRouter
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.LtiLaunchFragment
import com.instructure.student.router.RouteMatcher

class StudentEventRouter(private val activity: FragmentActivity, private val apiPrefs: ApiPrefs) : EventRouter {

    override fun openLtiScreen(canvasContext: CanvasContext?, url: String) {
        LtiLaunchFragment.routeLtiLaunchFragment(activity, canvasContext, url)
    }

    override fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?) {
        canvasContext?.let { RouteMatcher.route(activity, InternalWebviewFragment.makeRoute(it, url, false)) }
    }

    override fun openMediaFromWebView(mime: String, url: String, filename: String, canvasContext: CanvasContext?) {
        (activity as? BaseRouterActivity)?.openMedia(canvasContext, mime, url, filename)
    }

    override fun canRouteInternallyDelegate(url: String): Boolean {
        return RouteMatcher.canRouteInternally(activity, url, apiPrefs.domain, false)
    }

    override fun routeInternallyCallback(url: String) {
        RouteMatcher.canRouteInternally(activity, url, apiPrefs.domain, true)
    }

    override fun openEditEvent(scheduleItem: ScheduleItem) {
        TODO("Not yet implemented")
    }
}
