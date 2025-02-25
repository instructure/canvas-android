/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.router

import android.net.Uri
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.interactions.router.Route
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class EnabledTabsTest {
    private val mockUri: Uri = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val enabledTabs = EnabledTabsImpl(courseApi)

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isPathTabNotEnabled should return false when route is null`() = runTest {

        val result = enabledTabs.isPathTabNotEnabled(null)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled should return false when uri not contains uri or path`() = runTest {
        val route = Route(uri = null, routePath = null, courseId = null)

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled should return false when enabled tabs is null`() = runTest {
        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com"
        every { mockUri.pathSegments } returns emptyList()
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Fail()
        enabledTabs.initTabs()

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled should return false when enabled tabs is empty`() = runTest {
        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com"
        every { mockUri.pathSegments } returns emptyList()
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(emptyList())
        enabledTabs.initTabs()

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled should return false when uri not contains courseId`() = runTest {
        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com"
        every { mockUri.pathSegments } returns emptyList()

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled should return false value for valid details urls`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 1, tabs = listOf(Tab(tabId = "assignment", htmlUrl = "/courses/1/assignments"))),
            )
        )
        enabledTabs.initTabs()

        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/1/assignments/1"
        every { mockUri.pathSegments } returns listOf("courses", "1", "assignments", "1")

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled handle home urls correctly`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 1, tabs = listOf(Tab(tabId = "home", htmlUrl = "/courses/1"))),
            )
        )
        enabledTabs.initTabs()

        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/1"
        every { mockUri.pathSegments } returns listOf("courses", "1")

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled handle syllabus urls correctly with disabled tab`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 1, tabs = listOf(Tab(tabId = "assignments", htmlUrl = "/courses/1/assignments"))),
            )
        )
        enabledTabs.initTabs()

        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/1/assignments/syllabus"
        every { mockUri.pathSegments } returns listOf("courses", "1", "assignments", "syllabus")

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertTrue(result)
    }

    @Test
    fun `isPathTabNotEnabled handle syllabus urls correctly with enabled tab`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 1, tabs = listOf(Tab(tabId = "syllabus", htmlUrl = "/courses/1/assignments/syllabus"))),
            )
        )
        enabledTabs.initTabs()

        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/1/assignments/syllabus"
        every { mockUri.pathSegments } returns listOf("courses", "1", "assignments", "syllabus")

        val result = enabledTabs.isPathTabNotEnabled(route)

        assertFalse(result)
    }

    @Test
    fun `isPathTabNotEnabled can handle cross-course navigation`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 1, tabs = listOf(Tab(tabId = "assignments", htmlUrl = "/courses/1/assignments"))),
                Course(id = 2, tabs = listOf(Tab(tabId = "syllabus", htmlUrl = "/courses/2/assignments/syllabus"))),
            )
        )
        enabledTabs.initTabs()

        // Check course 1 disabled tab
        var route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/1/assignments/syllabus"
        every { mockUri.pathSegments } returns listOf("courses", "1", "assignments", "syllabus")
        var result = enabledTabs.isPathTabNotEnabled(route)
        assertTrue(result)

        // Check course 1 enabled tab
        route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/1/assignments"
        every { mockUri.pathSegments } returns listOf("courses", "1", "assignments")
        result = enabledTabs.isPathTabNotEnabled(route)
        assertFalse(result)

        // Check course 2 disabled tab
        route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/2/assignments"
        every { mockUri.pathSegments } returns listOf("courses", "2", "assignments")
        result = enabledTabs.isPathTabNotEnabled(route)
        assertTrue(result)

        // Check course 2 enabled tab
        route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/2/assignments/syllabus"
        every { mockUri.pathSegments } returns listOf("courses", "2", "assignments", "syllabus")
        result = enabledTabs.isPathTabNotEnabled(route)
        assertFalse(result)
    }

    @Test
    fun `replace ~ in consortia courseId to 0s`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 110000000000000012, tabs = listOf(Tab(tabId = "assignments", htmlUrl = "/courses/11~12/assignments"))),
            )
        )
        enabledTabs.initTabs()

        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/11~12/assignments"
        every { mockUri.pathSegments } returns listOf("courses", "11~12", "assignments")
        val result = enabledTabs.isPathTabNotEnabled(route)
        assert(result)
    }

    @Test
    fun `routing to grades is always allowed`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 1, tabs = listOf(Tab(tabId = "assignments", htmlUrl = "/courses/1/assignments"))),
            )
        )
        enabledTabs.initTabs()

        every { mockUri.path } returns "http://www.google.com/courses/1/grades"
        every { mockUri.pathSegments } returns listOf("courses", "1", "grades")

        val route = Route(uri = mockUri)
        val result = enabledTabs.isPathTabNotEnabled(route)
        assertFalse(result)
    }

    @Test
    fun `routing to discussions is always allowed`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(
            listOf(
                Course(id = 1, tabs = listOf(Tab(tabId = "assignments", htmlUrl = "/courses/1/assignments"))),
            )
        )
        enabledTabs.initTabs()

        val route = Route(uri = mockUri)
        every { mockUri.path } returns "http://www.google.com/courses/1/discussion_topics"
        every { mockUri.pathSegments } returns listOf("courses", "1", "discussion_topics")
        val result = enabledTabs.isPathTabNotEnabled(route)
        assertFalse(result)
    }
}