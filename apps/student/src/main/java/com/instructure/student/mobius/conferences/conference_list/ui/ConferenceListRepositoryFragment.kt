/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.mobius.conferences.conference_list.ui

import android.os.Bundle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CONFERENCE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.conferences.conference_list.ConferenceListRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView(url = "{canvasContext}/conferences")
@ScreenView(SCREEN_VIEW_CONFERENCE_LIST)
@AndroidEntryPoint
class ConferenceListRepositoryFragment : ConferenceListFragment() {

    @Inject
    lateinit var conferenceListRepository: ConferenceListRepository

    override fun getRepository() = conferenceListRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext): Route {
            return Route(null, ConferenceListRepositoryFragment::class.java, canvasContext, canvasContext.makeBundle())
        }

        private fun validRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): ConferenceListRepositoryFragment? {
            if (!validRoute(route)) return null
            return ConferenceListRepositoryFragment().withArgs(route.arguments)
        }
    }
}
