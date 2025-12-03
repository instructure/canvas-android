/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.dashboard.widget.courses

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetBehavior
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudentCoursesWidgetBehavior @Inject constructor(
    private val observeGradeVisibilityUseCase: ObserveGradeVisibilityUseCase,
    private val observeColorOverlayUseCase: ObserveColorOverlayUseCase,
    private val router: CoursesWidgetRouter
) : CoursesWidgetBehavior {

    override fun observeGradeVisibility(): Flow<Boolean> {
        return observeGradeVisibilityUseCase(Unit)
    }

    override fun observeColorOverlay(): Flow<Boolean> {
        return observeColorOverlayUseCase(Unit)
    }

    override fun onCourseClick(activity: FragmentActivity, course: Course) {
        router.routeToCourse(activity, course)
    }

    override fun onGroupClick(activity: FragmentActivity, group: Group) {
        router.routeToGroup(activity, group)
    }

    override fun onManageOfflineContent(activity: FragmentActivity, course: Course) {
        router.routeToManageOfflineContent(activity, course)
    }

    override fun onCustomizeCourse(activity: FragmentActivity, course: Course) {
        router.routeToCustomizeCourse(activity, course)
    }
}