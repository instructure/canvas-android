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
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TeacherCoursesWidgetBehaviorTest {

    private val router: CoursesWidgetRouter = mockk(relaxed = true)

    private lateinit var behavior: TeacherCoursesWidgetBehavior

    @Before
    fun setup() {
        behavior = TeacherCoursesWidgetBehavior(router = router)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `observeGradeVisibility always returns false for teacher`() = runTest {
        val result = behavior.observeGradeVisibility().first()

        assertFalse(result)
    }

    @Test
    fun `observeColorOverlay always returns true for teacher`() = runTest {
        val result = behavior.observeColorOverlay().first()

        assertTrue(result)
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
    fun `onAllCoursesClicked delegates to router`() {
        val activity: FragmentActivity = mockk()

        behavior.onAllCoursesClicked(activity)

        verify { router.routeToAllCourses(activity) }
    }
}