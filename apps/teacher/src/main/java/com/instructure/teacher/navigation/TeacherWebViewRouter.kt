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
package com.instructure.teacher.navigation

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.teacher.fragments.FullscreenInternalWebViewFragment
import com.instructure.teacher.fragments.InternalWebViewFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherWebViewRouter(val activity: FragmentActivity) : WebViewRouter {

    override fun canRouteInternally(url: String, routeIfPossible: Boolean): Boolean {
        return RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = routeIfPossible)
    }

    override fun routeInternally(url: String) {
        RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = true)
    }

    override fun openMedia(url: String) {
        RouteMatcher.openMedia(activity, url)
    }

    override fun routeExternally(url: String) {
        val bundle = InternalWebViewFragment.makeBundle(url, url, false, "")
        RouteMatcher.route(
            activity, Route(
                FullscreenInternalWebViewFragment::class.java,
                CanvasContext.emptyUserContext(), bundle
            )
        )
    }
}