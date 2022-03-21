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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.student.R
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.fragment_course_settings.*
import kotlinx.android.synthetic.main.fragment_course_settings.view.*

@ScreenView(SCREEN_VIEW_COURSE_SETTINGS)
class CourseSettingsFragment : ParentFragment() {

    var course: Course by ParcelableArg(key = Const.CANVAS_CONTEXT)

    override fun title(): String = getString(R.string.settings)

    override fun applyTheme() {
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, course)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_course_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        courseName.text = course.name
        courseCode.text = course.courseCode
        license.text = course.license?.prettyString
        visibility.text = getString(if (course.isPublic) R.string.publiclyAvailable else R.string.privatelyAvailable)
        startLayout.setVisible(course.startDate != null).startDate.text =
                DateHelper.dateToDayMonthYearString(requireContext(), course.startDate)
        endLayout.setVisible(course.endDate != null).endDate.text =
                DateHelper.dateToDayMonthYearString(requireContext(), course.endDate)
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext) = Route(CourseSettingsFragment::class.java, canvasContext)

        private fun validateRoute(route: Route) = route.canvasContext is Course

        fun newInstance(route: Route): CourseSettingsFragment? {
            if (!validateRoute(route)) return null
            return CourseSettingsFragment().withArgs(route.canvasContext!!.makeBundle())
        }

    }
}
