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
package com.instructure.student.mobius.assignmentDetails.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.student.mobius.assignmentDetails.*
import com.instructure.student.mobius.common.ui.MobiusFragment

@PageView(url = "{canvasContext}/assignments/{assignmentId}")
class AssignmentDetailsFragment :
    MobiusFragment<AssignmentDetailsModel, AssignmentDetailsEvent, AssignmentDetailsEffect, AssignmentDetailsView, AssignmentDetailsViewState>() {

    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)

    @get:PageViewUrlParam(name = "assignmentId")
    val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)

    override fun makeEffectHandler() = AssignmentDetailsEffectHandler(requireContext())

    override fun makeUpdate() = AssignmentDetailsUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        AssignmentDetailsView(canvasContext, inflater, parent)

    override fun makePresenter() = AssignmentDetailsPresenter

    override fun makeInitModel() = AssignmentDetailsModel(assignmentId, canvasContext)

    companion object {

        @JvmStatic
        fun makeRoute(course: CanvasContext, assignmentId: Long): Route {
            val bundle = course.makeBundle { putLong(Const.ASSIGNMENT_ID, assignmentId) }
            return Route(null, AssignmentDetailsFragment::class.java, course, bundle)
        }

        @JvmStatic
        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course &&
                    (route.arguments.containsKey(Const.ASSIGNMENT_ID) ||
                            route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID))
        }

        @JvmStatic
        fun newInstance(route: Route): AssignmentDetailsFragment? {
            if (!validRoute(route)) return null

            // If routed from a URL, set the bundle's assignment ID from the url value
            if (route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID)) {
                val assignmentId = route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLong() ?: -1
                route.arguments.putLong(Const.ASSIGNMENT_ID, assignmentId)
            }

            return AssignmentDetailsFragment().withArgs(route.arguments)
        }

    }

}
