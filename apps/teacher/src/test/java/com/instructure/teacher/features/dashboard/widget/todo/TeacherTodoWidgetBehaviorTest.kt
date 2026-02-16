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

package com.instructure.teacher.features.dashboard.widget.todo

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetRouter
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class TeacherTodoWidgetBehaviorTest {

    private val router: TodoWidgetRouter = mockk(relaxed = true)
    private lateinit var behavior: TeacherTodoWidgetBehavior

    @Before
    fun setup() {
        behavior = TeacherTodoWidgetBehavior(router)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `onTodoClick routes to todo with correct URL`() {
        val activity = mockk<FragmentActivity>()
        val htmlUrl = "https://instructure.com/courses/123/assignments/456"

        behavior.onTodoClick(activity, htmlUrl)

        verify { router.routeToTodo(activity, htmlUrl) }
    }

    @Test
    fun `onTodoClick routes to todo with different URLs`() {
        val activity = mockk<FragmentActivity>()
        val url1 = "https://instructure.com/courses/111/quizzes/222"
        val url2 = "https://instructure.com/courses/333/discussion_topics/444"

        behavior.onTodoClick(activity, url1)
        behavior.onTodoClick(activity, url2)

        verify { router.routeToTodo(activity, url1) }
        verify { router.routeToTodo(activity, url2) }
    }

    @Test
    fun `onAddTodoClick routes to create todo with initial date`() {
        val activity = mockk<FragmentActivity>()
        val initialDateString = "2025-02-15"

        behavior.onAddTodoClick(activity, initialDateString)

        verify { router.routeToCreateTodo(activity, initialDateString) }
    }

    @Test
    fun `onAddTodoClick routes to create todo without initial date`() {
        val activity = mockk<FragmentActivity>()

        behavior.onAddTodoClick(activity, null)

        verify { router.routeToCreateTodo(activity, null) }
    }

    @Test
    fun `onAddTodoClick routes to create todo with different dates`() {
        val activity = mockk<FragmentActivity>()
        val date1 = "2025-01-01"
        val date2 = "2025-12-31"

        behavior.onAddTodoClick(activity, date1)
        behavior.onAddTodoClick(activity, date2)

        verify { router.routeToCreateTodo(activity, date1) }
        verify { router.routeToCreateTodo(activity, date2) }
    }

    @Test
    fun `updateWidget does not throw exception`() {
        // The method is not yet implemented but should not throw
        behavior.updateWidget(forceRefresh = true)
        behavior.updateWidget(forceRefresh = false)
        behavior.updateWidget()
    }
}