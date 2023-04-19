/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_UNSUPPORTED_TAB
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R

@ScreenView(SCREEN_VIEW_UNSUPPORTED_TAB)
class UnsupportedTabFragment : InternalWebviewFragment() {

    override fun applyTheme() {
        super.applyTheme()
        binding.toolbar.setupAsBackButton {
            navigation?.popCurrentFragment()
        }
    }

    companion object {

        fun newInstance(route: Route) = if (validRoute(route)) {
            UnsupportedTabFragment().apply {
                val info = extractInfo(route.canvasContext!!, route.tabId!!)
                arguments = makeBundle(route.canvasContext!!, info.first, info.second, true, true, false)
            }

        } else null

        fun makeRoute(canvasContext: CanvasContext, tabId: String): Route {
            val info = extractInfo(canvasContext, tabId)
            val bundle = makeRoute(info.first, info.second, true, true, false)
            return Route(UnsupportedTabFragment::class.java, canvasContext, bundle, tabId)
        }

        private fun validRoute(route: Route) = route.canvasContext != null && route.tabId != null

        private fun extractInfo(canvasContext: CanvasContext, tabId: String): Pair<String, String> {
            var url = ApiPrefs.fullDomain
            var featureTitle = ""

            when {
                tabId.equals(Tab.CONFERENCES_ID, ignoreCase = true) -> {
                    url += canvasContext.toAPIString() + "/conferences"
                    featureTitle = ContextKeeper.appContext.getString(R.string.conferences)
                }
                tabId.equals(Tab.COLLABORATIONS_ID, ignoreCase = true) -> {
                    url += canvasContext.toAPIString() + "/collaborations"
                    featureTitle = ContextKeeper.appContext.getString(R.string.collaborations)
                }
                tabId.equals(Tab.OUTCOMES_ID, ignoreCase = true) -> {
                    url += canvasContext.toAPIString() + "/outcomes"
                    featureTitle = ContextKeeper.appContext.getString(R.string.outcomes)
                }
            }

            return Pair(url, featureTitle)
        }
    }
}
