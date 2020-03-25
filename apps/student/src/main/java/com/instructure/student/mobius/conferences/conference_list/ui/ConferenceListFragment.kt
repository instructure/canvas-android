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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEffect
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEffectHandler
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEvent
import com.instructure.student.mobius.conferences.conference_list.ConferenceListModel
import com.instructure.student.mobius.conferences.conference_list.ConferenceListPresenter
import com.instructure.student.mobius.conferences.conference_list.ConferenceListUpdate

class ConferenceListFragment :
    MobiusFragment<ConferenceListModel, ConferenceListEvent, ConferenceListEffect, ConferenceListView, ConferenceListViewState>() {

    val canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    override fun makeUpdate() =
        ConferenceListUpdate()

    override fun makePresenter() =
        ConferenceListPresenter

    override fun makeEffectHandler() =
        ConferenceListEffectHandler()

    override fun makeInitModel() =
        ConferenceListModel(canvasContext)

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        ConferenceListView(
            canvasContext,
            inflater,
            parent
        )

    companion object {
        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext): Route {
            return Route(null, ConferenceListFragment::class.java, canvasContext, canvasContext.makeBundle())
        }

        private fun validRoute(route: Route) = route.canvasContext != null

        @JvmStatic
        fun newInstance(route: Route): ConferenceListFragment? {
            if (!validRoute(
                    route
                )
            ) return null
            return ConferenceListFragment()
                .withArgs(route.arguments)
        }
    }
}
