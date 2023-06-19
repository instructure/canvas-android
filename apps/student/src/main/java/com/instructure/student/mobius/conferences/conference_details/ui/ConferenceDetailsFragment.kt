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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CONFERENCE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.databinding.FragmentConferenceDetailsBinding
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.conferences.conference_details.*

@PageView(url = "{canvasContext}/conferences/{conferenceId}")
@ScreenView(SCREEN_VIEW_CONFERENCE_DETAILS)
class ConferenceDetailsFragment :
    MobiusFragment<ConferenceDetailsModel, ConferenceDetailsEvent, ConferenceDetailsEffect, ConferenceDetailsView, ConferenceDetailsViewState, FragmentConferenceDetailsBinding>() {

    val canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    val conference by ParcelableArg<Conference>(key = Const.CONFERENCE)

    override fun makeUpdate() =
        ConferenceDetailsUpdate()

    override fun makePresenter() =
        ConferenceDetailsPresenter

    override fun makeEffectHandler() =
        ConferenceDetailsEffectHandler()

    override fun makeInitModel() =
        ConferenceDetailsModel(canvasContext, conference)

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        ConferenceDetailsView(
            canvasContext,
            inflater,
            parent
        )

    @PageViewUrlParam("conferenceId")
    fun getConferenceId() = conference.id

    companion object {
        fun makeRoute(canvasContext: CanvasContext, conference: Conference): Route {
            val bundle = canvasContext.makeBundle {
                putParcelable(Const.CONFERENCE, conference)
            }
            return Route(null, ConferenceDetailsFragment::class.java, canvasContext, bundle)
        }

        private fun validRoute(route: Route) =
            route.canvasContext != null && route.arguments.containsKey(Const.CONFERENCE)

        fun newInstance(route: Route): ConferenceDetailsFragment? {
            if (!validRoute(route)) return null
            return ConferenceDetailsFragment()
                .withArgs(route.arguments)
        }
    }
}
