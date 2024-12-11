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
package com.instructure.student.mobius.conferences.conference_details.ui

import android.os.Bundle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CONFERENCE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@PageView(url = "{canvasContext}/conferences/{conferenceId}")
@ScreenView(SCREEN_VIEW_CONFERENCE_DETAILS)
class ConferenceDetailsRepositoryFragment : ConferenceDetailsFragment() {

    @Inject
    lateinit var conferenceDetailsRepository: ConferenceDetailsRepository

    override fun getRepository() = conferenceDetailsRepository

    @PageViewUrlParam("conferenceId")
    fun getConferenceId() = conference.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext, conference: Conference): Route {
            val bundle = canvasContext.makeBundle {
                putParcelable(Const.CONFERENCE, conference)
            }
            return Route(null, ConferenceDetailsRepositoryFragment::class.java, canvasContext, bundle)
        }

        private fun validRoute(route: Route) =
            route.canvasContext != null && route.arguments.containsKey(Const.CONFERENCE)

        fun newInstance(route: Route): ConferenceDetailsRepositoryFragment? {
            if (!validRoute(route)) return null
            return ConferenceDetailsRepositoryFragment().withArgs(route.arguments)
        }
    }
}
