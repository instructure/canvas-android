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
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class StudentCoursesWidgetBehaviorTest {

    private val router: CoursesWidgetRouter = mockk(relaxed = true)

    private lateinit var behavior: StudentCoursesWidgetBehavior

    @Before
    fun setup() {
        behavior = StudentCoursesWidgetBehavior(
            router = router
        )
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `onCourseClick delegates to router`() {
        val activity: FragmentActivity = mockk()
        val course = Course(id = 1, name = "Test Course")

        behavior.onCourseClick(activity, course)

        verify { router.routeToCourse(activity, course) }
    }

    @Test
    fun `onGroupClick delegates to router`() {
        val activity: FragmentActivity = mockk()
        val group = Group(id = 1, name = "Test Group")

        behavior.onGroupClick(activity, group)

        verify { router.routeToGroup(activity, group) }
    }

    @Test
    fun `onManageOfflineContent delegates to router`() {
        val activity: FragmentActivity = mockk()
        val course = Course(id = 1, name = "Test Course")

        behavior.onManageOfflineContent(activity, course)

        verify { router.routeToManageOfflineContent(activity, course) }
    }

    @Test
    fun `onCustomizeCourse delegates to router`() {
        val activity: FragmentActivity = mockk()
        val course = Course(id = 1, name = "Test Course")

        behavior.onCustomizeCourse(activity, course)

        verify { router.routeToCustomizeCourse(activity, course) }
    }

    @Test
    fun `onAllCoursesClicked delegates to router`() {
        val activity: FragmentActivity = mockk()

        behavior.onAllCoursesClicked(activity)

        verify { router.routeToAllCourses(activity) }
    }

    @Test
    fun `onAnnouncementClick with single announcement routes to announcement details`() {
        val activity: FragmentActivity = mockk()
        val course = Course(id = 1, name = "Test Course")
        val announcement = DiscussionTopicHeader(id = 1, title = "Test Announcement")
        val announcements = listOf(announcement)

        behavior.onAnnouncementClick(activity, course, announcements)

        verify { router.routeToAnnouncement(activity, course, announcement) }
    }

    @Test
    fun `onAnnouncementClick with multiple announcements routes to announcement list`() {
        val activity: FragmentActivity = mockk()
        val course = Course(id = 1, name = "Test Course")
        val announcements = listOf(
            DiscussionTopicHeader(id = 1, title = "Announcement 1"),
            DiscussionTopicHeader(id = 2, title = "Announcement 2")
        )

        behavior.onAnnouncementClick(activity, course, announcements)

        verify { router.routeToAnnouncementList(activity, course) }
    }

    @Test
    fun `onAnnouncementClick with empty list routes to announcement list`() {
        val activity: FragmentActivity = mockk()
        val course = Course(id = 1, name = "Test Course")
        val announcements = emptyList<DiscussionTopicHeader>()

        behavior.onAnnouncementClick(activity, course, announcements)

        verify { router.routeToAnnouncementList(activity, course) }
    }

    @Test
    fun `onGroupMessageClick delegates to router`() {
        val activity: FragmentActivity = mockk()
        val group = Group(id = 1, name = "Test Group")

        behavior.onGroupMessageClick(activity, group)

        verify { router.routeToGroupMessage(activity, group) }
    }
}