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

package com.instructure.teacher.features.dashboard.widget.courses

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetBehavior
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class TeacherCoursesWidgetBehavior @Inject constructor(
    private val router: CoursesWidgetRouter
) : CoursesWidgetBehavior {

    override fun observeGradeVisibility(): Flow<Boolean> {
        return flowOf(false)
    }

    override fun observeColorOverlay(): Flow<Boolean> {
        return flowOf(true)
    }

    override fun onCourseClick(activity: FragmentActivity, course: Course) {
        router.routeToCourse(activity, course)
    }

    override fun onGroupClick(activity: FragmentActivity, group: Group) {
        router.routeToGroup(activity, group)
    }

    override fun onManageOfflineContent(activity: FragmentActivity, course: Course) {
        throw NotImplementedError()
    }

    override fun onCustomizeCourse(activity: FragmentActivity, course: Course) {
        throw NotImplementedError()
    }

    override fun onAllCoursesClicked(activity: FragmentActivity) {
        router.routeToAllCourses(activity)
    }

    override fun onAnnouncementClick(activity: FragmentActivity, course: Course, announcements: List<DiscussionTopicHeader>) {
        // TODO: Implement announcement click handling
    }

    override fun onGroupMessageClick(activity: FragmentActivity, group: Group) {
        // TODO: Implement group message click handling
    }
}
