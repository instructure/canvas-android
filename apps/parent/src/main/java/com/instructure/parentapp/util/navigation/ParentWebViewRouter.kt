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
package com.instructure.parentapp.util.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.toast
import com.instructure.parentapp.R

class ParentWebViewRouter(
    private val activity: FragmentActivity,
    private val navigation: Navigation
) : WebViewRouter {

    override fun canRouteInternally(url: String, routeIfPossible: Boolean): Boolean {
        return navigation.canNavigate(activity, url, routeIfPossible)
    }

    override fun routeInternally(url: String, extras: Bundle?) {
        navigation.canNavigate(activity, url, true)
    }

    override fun openMedia(url: String, mime: String, filename: String, canvasContext: CanvasContext?) = safeNavigate(url) {
        navigation.navigate(activity, navigation.internalWebViewRoute(url, url))
    }

    override fun routeExternally(url: String) = safeNavigate(url) {
        navigation.navigate(activity, navigation.internalWebViewRoute(url, url))
    }

    override fun openLtiScreen(canvasContext: CanvasContext?, url: String) = safeNavigate(url) {
        navigation.navigate(activity, navigation.ltiLaunchRoute(url, activity.getString(R.string.utils_externalToolTitle), sessionlessLaunch = true))
    }

    override fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?) = safeNavigate(url) {
        navigation.navigate(activity, navigation.internalWebViewRoute(url, canvasContext?.name ?: url))
    }

    private fun safeNavigate(url: String, route: () -> Unit) = try {
        route()
    } catch (e: Exception) {
        navigateToExternalBrowser(url)
    }

    private fun navigateToExternalBrowser(url: String) = try {
        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(urlIntent)
    } catch (e: ActivityNotFoundException) {
        activity.toast(R.string.inboxMessageFailedToOpenUrl)
    }
}
