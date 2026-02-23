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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.databinding.FragmentCourseSettingsBinding

@PageView
@ScreenView(SCREEN_VIEW_COURSE_SETTINGS)
class CourseSettingsFragment : ParentFragment() {

    private val binding by viewBinding(FragmentCourseSettingsBinding::bind)

    var course: Course by ParcelableArg(key = Const.CANVAS_CONTEXT)

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() = "courses/${course.id}/settings"

    override fun title(): String = getString(R.string.settings)

    override fun applyTheme() {
        binding.toolbar.title = title()
        binding.toolbar.setupAsBackButton(this)
        binding.toolbar.applyTopSystemBarInsets()
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, course)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_course_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        courseName.text = course.name
        courseCode.text = course.courseCode
        license.text = course.license?.prettyString
        visibility.text = getString(if (course.isPublic) R.string.publiclyAvailable else R.string.privatelyAvailable)
        startLayout.setVisible(course.startDate != null)
        startDate.text = DateHelper.dateToDayMonthYearString(requireContext(), course.startDate)
        endLayout.setVisible(course.endDate != null)
        endDate.text = DateHelper.dateToDayMonthYearString(requireContext(), course.endDate)
        scrollView.applyBottomSystemBarInsets()
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
