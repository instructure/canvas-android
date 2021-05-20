/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.mobius.elementary

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter
import com.instructure.student.fragment.AnnouncementListFragment
import com.instructure.student.router.RouteMatcher

class StudentHomeroomRouter(private val activity: FragmentActivity) : HomeroomRouter {

    override fun canRouteInternally(url: String): Boolean {
        return RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = false, allowUnsupported = false)
    }

    override fun routeInternally(url: String) {
        RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = true, allowUnsupported = false)
    }

    override fun openMedia(url: String) {
        RouteMatcher.openMedia(activity, url)
    }

    override fun openAnnouncements(canvasContext: CanvasContext) {
        val route = AnnouncementListFragment.makeRoute(canvasContext)
        RouteMatcher.route(activity, route)
    }
}