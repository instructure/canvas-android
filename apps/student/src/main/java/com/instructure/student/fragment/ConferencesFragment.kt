/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import kotlinx.android.synthetic.main.fragment_webview.*

class ConferencesFragment : InternalWebviewFragment() {

    private fun isJoinUrl(url: String?): Boolean = url?.contains("bigbluebutton/api/join") == true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = canvasWebView.canvasWebViewClientCallback
        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback by callback {
            override fun canRouteInternallyDelegate(url: String?): Boolean {
                return isJoinUrl(url) || callback.canRouteInternallyDelegate(url)
            }

            override fun routeInternallyCallback(url: String?) {
                if (isJoinUrl(url)) {
                    // Launch in custom browser tab
                    CustomTabsIntent.Builder()
                        .setToolbarColor(canvasContext.color)
                        .setShowTitle(true)
                        .build()
                        .launchUrl(requireContext(), Uri.parse(url))
                } else {
                    callback.routeInternallyCallback(url)
                }
            }
        }
    }

    companion object {

        fun newInstance(route: Route) = if (validRoute(route)) {
            ConferencesFragment().withArgs(route.arguments)
        } else null

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext): Route {
            val url = ApiPrefs.fullDomain + canvasContext.toAPIString() + "/conferences"
            val featureTitle = ContextKeeper.appContext.getString(R.string.conferences)
            val bundle = makeRoute(url, featureTitle, true, true, false)
            return Route(ConferencesFragment::class.java, canvasContext, bundle)
        }

        private fun validRoute(route: Route) = route.canvasContext != null

    }
}
