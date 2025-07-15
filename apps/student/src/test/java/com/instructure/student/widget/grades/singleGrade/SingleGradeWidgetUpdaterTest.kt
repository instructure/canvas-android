/* Copyright (C) 2025 - present Instructure, Inc.
*
*     Licensed under the Apache License, Version 2.0 (the "License");
*     you may not use this file except in compliance with the License.
*     You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*     Unless required by applicable law or agreed to in writing, software
*     distributed under the License is distributed on an "AS IS" BASIS,
*     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*     See the License for the specific language governing permissions and
*     limitations under the License.
*/
package com.instructure.student.widget.grades.singleGrade

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.grades.GradesWidgetRepository
import com.instructure.student.widget.grades.courseselector.CourseSelectorActivity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SingleGradeWidgetUpdaterTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: GradesWidgetRepository = mockk()
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val glanceManager: GlanceAppWidgetManager = mockk()
    private val glanceId: GlanceId = mockk()
    private val widgetId = 42
    private lateinit var updater: SingleGradeWidgetUpdater

    @Before
    fun setup() {
        every { apiPrefs.user } returns mockk()
        mockkConstructor(GlanceAppWidgetManager::class)
        every { glanceManager.getGlanceIdBy(any<Int>()) } returns glanceId
        updater = SingleGradeWidgetUpdater(repository, apiPrefs, glanceManager)
    }

    @Test
    fun `emits NotLoggedIn when user is null`() = runTest {
        every { apiPrefs.user } returns null

        val events = mutableListOf<Any>()
        backgroundScope.launch(testDispatcher) {
            updater.events.take(2).toList(events)
        }
        updater.updateData(listOf(widgetId))

        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateAllState && it.state.state == WidgetState.NotLoggedIn })
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateUi })
    }

    @Test
    fun `Emits NotLoggedIn state when api call gets authorization error`() = runTest {
        coEvery { repository.getCoursesWithGradingScheme(any()) } returns DataResult.Fail(Failure.Authorization())

        val events = mutableListOf<Any>()
        backgroundScope.launch(testDispatcher) {
            updater.events.take(2).toList(events)
        }
        updater.updateData(listOf(widgetId))

        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateAllState && it.state.state == WidgetState.NotLoggedIn })
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateUi })
    }

    @Test
    fun `emits Error when repository returns empty list`() = runTest {
        coEvery { repository.getCoursesWithGradingScheme(any()) } returns DataResult.Success(emptyList())

        val events = mutableListOf<Any>()
        backgroundScope.launch(testDispatcher) {
            updater.events.take(2).toList(events)
        }
        updater.updateData(listOf(widgetId))

        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateAllState && it.state.state == WidgetState.Error })
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateUi })
    }

    @Test
    fun `emits Content when matching course is found`() = runTest {
        every { apiPrefs.fullDomain } returns ""
        val themedColor = mockk<ThemedColor>(relaxed = true)
        every { themedColor.light } returns 0
        every { themedColor.dark } returns 0
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns themedColor
        val course = mockk<Course>(relaxed = true)
        val courseId = 123L
        every { course.id } returns courseId
        coEvery { repository.getCoursesWithGradingScheme(any()) } returns DataResult.Success(listOf(course))
        mockkObject(StudentPrefs)
        every { StudentPrefs.getLong(CourseSelectorActivity.WIDGET_COURSE_ID_PREFIX + widgetId, -1) } returns courseId

        val events = mutableListOf<Any>()
        backgroundScope.launch(testDispatcher) {
            updater.events.take(2).toList(events)
        }
        updater.updateData(listOf(widgetId))

        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateState && it.state.state == WidgetState.Content })
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateUi })
    }

    @Test
    fun `emits Empty when no matching course is found`() = runTest {
        val course = mockk<Course>(relaxed = true)
        val courseId = 123L
        every { course.id } returns courseId
        coEvery { repository.getCoursesWithGradingScheme(any()) } returns DataResult.Success(listOf(course))
        mockkObject(StudentPrefs)
        every { StudentPrefs.getLong(CourseSelectorActivity.WIDGET_COURSE_ID_PREFIX + widgetId, -1) } returns -1

        val events = mutableListOf<Any>()
        backgroundScope.launch(testDispatcher) {
            updater.events.take(2).toList(events)
        }

        updater.updateData(listOf(widgetId))
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateState && it.state.state == WidgetState.Empty })
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateUi })
    }

    @Test
    fun `emits Error when repository throws`() = runTest {
        coEvery { repository.getCoursesWithGradingScheme(any()) } throws RuntimeException("fail")

        val events = mutableListOf<Any>()
        backgroundScope.launch(testDispatcher) {
            updater.events.take(2).toList(events)
        }
        updater.updateData(listOf(widgetId))

        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateAllState && it.state.state == WidgetState.Error })
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateUi })
    }

    @Test
    fun `emits Loading when showLoading is true`() = runTest {
        coEvery { repository.getCoursesWithGradingScheme(any()) } returns DataResult.Success(emptyList())

        val events = mutableListOf<Any>()
        backgroundScope.launch(testDispatcher) {
            updater.events.take(3).toList(events)
        }
        updater.updateData(listOf(widgetId), showLoading = true)

        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateAllState && it.state.state == WidgetState.Loading })
        assertTrue(events.any { it is SingleGradeWidgetAction.UpdateUi })
    }
}
