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
package com.instructure.student.navigation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.LtiLaunchFragment
import com.instructure.student.router.RouteMatcher

class StudentWebViewRouter(val activity: FragmentActivity) : WebViewRouter {

    override fun canRouteInternally(url: String, routeIfPossible: Boolean): Boolean {
        return RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = routeIfPossible, allowUnsupported = false)
    }

    override fun routeInternally(url: String, extras: Bundle?) {
        if (extras != null) {
            RouteMatcher.routeUrl(activity, url, ApiPrefs.domain, extras)
        } else {
            RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = true, allowUnsupported = false)
        }
    }

    override fun openMedia(url: String, mime: String, filename: String, canvasContext: CanvasContext?) {
        if (canvasContext != null && activity is BaseRouterActivity) {
            activity.openMedia(canvasContext, mime, url, filename, null)
        } else {
            RouteMatcher.openMedia(activity, url)
        }
    }

    override fun routeExternally(url: String) {
        RouteMatcher.route(activity, InternalWebviewFragment.makeRoute(url, url, false, ""))
    }

    override fun openLtiScreen(canvasContext: CanvasContext?, url: String) {
        LtiLaunchFragment.routeLtiLaunchFragment(activity, canvasContext, url)
    }

    override fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?) {
        canvasContext?.let { RouteMatcher.route(activity, InternalWebviewFragment.makeRoute(it, url, false)) }
    }
}